/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
 */

package org.graalvm.benchmark.photon;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.dylibso.chicory.runtime.ByteArrayMemory;
import com.dylibso.chicory.runtime.ByteBufferMemory;
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

import com.dylibso.chicory.runtime.ExportFunction;
import com.dylibso.chicory.runtime.Instance;

@Warmup(iterations = 3)
@Measurement(iterations = 3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Fork(1)
public class ChicoryPhotonTest {
  
    @State(Scope.Benchmark)
    public static class ChicoryFixture {
        private static final String BYTE_BUFFER = "byte-buffer";
        private static final String BYTE_ARRAY = "byte-array";

        public ExportFunction benchmarkFn;

        @Param({
            BYTE_ARRAY,
            BYTE_BUFFER
        })
        private String mode;

        @Setup(Level.Trial)
        public void doSetup() {
            // create the module and instantiate (the module) and connect our imports
            InputStream wasmFileStream = ChicoryPhotonTest.class.getResourceAsStream(PhotonTestParams.WASM_FILENAME);
            Instance instance = null;;
           
            switch (mode) {
                case BYTE_ARRAY:
                    instance = Instance.builder(PhotonModule.load())
                            .withMachineFactory(PhotonModule::create)
                            .withMemoryFactory(ByteArrayMemory::new)
                            .build();
                    break;
                case BYTE_BUFFER:
                    instance = Instance.builder(PhotonModule.load())
                            .withMachineFactory(PhotonModule::create)
                            .withMemoryFactory(ByteBufferMemory::new)
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