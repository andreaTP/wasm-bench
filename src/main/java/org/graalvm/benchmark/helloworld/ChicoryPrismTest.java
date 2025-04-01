/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

 package org.graalvm.benchmark.helloworld;

import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.runtime.ByteArrayMemory;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.Parser;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.prism.PrismModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 3)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
public class ChicoryPrismTest {
    private static final String INTERPRETER = "interpreter";
    private static final String PRECOMPILED_AOT = "precompiled-aot";

    @State(Scope.Benchmark)
    public static class ChicoryFixture {
        private WasiPreview1 wasi;
        public Instance instance;

        @Param({
                INTERPRETER,
                PRECOMPILED_AOT
        })
        private String mode;

        @Setup(Level.Trial)
        public void doSetup() {
            // create the module and instantiate (the module) and connect our imports
            wasi = WasiPreview1.builder().build();
            var imports = ImportValues.builder().addFunction(wasi.toHostFunctions()).build();
            switch (mode) {
                case INTERPRETER:
                    instance = Instance.builder(Parser.parse(Path.of("./src/main/resources/prism.wasm")))
                            .withImportValues(imports)
                            .build();
                    break;
                case PRECOMPILED_AOT:
                    instance = Instance.builder(PrismModule.load())
                            .withMemoryFactory(ByteArrayMemory::new)
                            .withMachineFactory(PrismModule::create)
                            .withImportValues(ImportValues.builder().addFunction(wasi.toHostFunctions()).build())
                            .build();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Chicory mode " + mode);
            }
        }

        @TearDown(Level.Trial)
        public void close() { wasi.close(); }
    }

    private final static String source = "puts \"h\ne\nl\nl\no\n\"";
    private final static byte[] sourceBytes = source.getBytes(StandardCharsets.US_ASCII);

    private static final byte[] packedOptions = new byte[] { 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    @Benchmark
    /*
     * Tests a simple HelloWorld Go function, including all of the calls necessary to obtain
     * required functions.
     */    
    public void chicoryTest(ChicoryFixture fixture, Blackhole blackhole) throws IOException {
        var memory = fixture.instance.memory();
        var calloc = fixture.instance.export("calloc");
        var free = fixture.instance.export("free");
        var pmSerializeParse = fixture.instance.export("pm_serialize_parse");
        var pmBufferInit = fixture.instance.export("pm_buffer_init");
        var pmBufferSizeof = fixture.instance.export("pm_buffer_sizeof");
        var pmBufferValue = fixture.instance.export("pm_buffer_value");
        var pmBufferLength = fixture.instance.export("pm_buffer_length");

        var sourcePointer = calloc.apply(1, sourceBytes.length);
        memory.write((int) sourcePointer[0], sourceBytes);

        var optionsPointer = calloc.apply(1, packedOptions.length);
        memory.write((int) optionsPointer[0], packedOptions);

        var bufferPointer = calloc.apply(pmBufferSizeof.apply()[0], 1);
        pmBufferInit.apply(bufferPointer);

        pmSerializeParse.apply(
                bufferPointer[0], sourcePointer[0], source.length(), optionsPointer[0]);

        var resultPtr = (int) pmBufferValue.apply(bufferPointer[0])[0];
        var resultLen = (int) pmBufferLength.apply(bufferPointer[0])[0];

        var result = memory.readBytes(resultPtr, resultLen);

        free.apply(bufferPointer[0]);
        free.apply(sourcePointer[0]);
        free.apply(optionsPointer[0]);
        free.apply(resultPtr);

        blackhole.consume(result);
    }
}
