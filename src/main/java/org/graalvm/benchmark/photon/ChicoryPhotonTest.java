/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package org.graalvm.benchmark.photon;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import com.dylibso.chicory.experimental.aot.AotMachine;
import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasm.Parser;

@Warmup(iterations = 3)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
public class ChicoryPhotonTest {
  
    @State(Scope.Benchmark)
    public static class ChicoryFixture {
//        private static final String INTERPRETER = "interpreter";
        private static final String RUNTIME_AOT = "runtime-aot";
        private static final String PRECOMPILED_AOT = "precompiled-aot";

        public ExportFunction benchmarkFn;

        @Param({
            RUNTIME_AOT,
            PRECOMPILED_AOT,
//            INTERPRETER
        })
        private String mode;

        @Setup(Level.Trial)
        public void doSetup() {
            // create the module and instantiate (the module) and connect our imports
            InputStream wasmFileStream = ChicoryPhotonTest.class.getResourceAsStream(PhotonTestParams.WASM_FILENAME);
            Instance instance = null;;
           
            switch (mode) {
//                case INTERPRETER:
//                    instance = Instance.builder(Parser.parse(wasmFileStream)).build();
//                    break;
                case RUNTIME_AOT:
                    instance = Instance.builder(Parser.parse(wasmFileStream))
                            .withMachineFactory(AotMachine::new)
                            .build();
                    break;
                case PRECOMPILED_AOT:
                    instance = Instance.builder(PhotonModule.load())
                            .withMachineFactory(PhotonModule::create)
                            .build();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown Chicory mode " + mode);
            }
            benchmarkFn = instance.export(PhotonTestParams.WASM_FUNCTION);
        }
    }

    @Benchmark
    public void chicoryTest(ChicoryFixture fixture, Blackhole blackhole) throws IOException {
        final int hash = (int)fixture.benchmarkFn.apply()[0];
        blackhole.consume(hash);   
    }
}