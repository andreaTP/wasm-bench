// /*
//  * Copyright (c) 2024, Oracle and/or its affiliates.
//  *
//  * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
//  */

//  package org.graalvm.benchmark.helloworld;

// import com.dylibso.chicory.experimental.aot.AotMachine;
// import com.dylibso.chicory.runtime.ExportFunction;
// import com.dylibso.chicory.runtime.ImportValues;
// import com.dylibso.chicory.runtime.Instance;
// import com.dylibso.chicory.runtime.Memory;
// import com.dylibso.chicory.wasi.WasiPreview1;
// import com.dylibso.chicory.wasm.Parser;
// import org.openjdk.jmh.annotations.*;
// import org.openjdk.jmh.infra.Blackhole;

// import java.io.IOException;
// import java.io.InputStream;
// import java.util.concurrent.TimeUnit;

// @Warmup(iterations = 3)
// @Measurement(iterations = 3)
// @BenchmarkMode(Mode.Throughput)
// @OutputTimeUnit(TimeUnit.SECONDS)
// @Fork(1)
  
// public class ChicoryHelloTest {
//     // private static final String INTERPRETER = "interpreter";
//     private static final String RUNTIME_AOT = "runtime-aot";
//     private static final String PRECOMPILED_AOT = "precompiled-aot";

//     @State(Scope.Benchmark)
//     public static class ChicoryFixture {
//         private WasiPreview1 wasi;
//         public Instance instance;

//         @Param({
//                 // INTERPRETER,
//                 RUNTIME_AOT,
//                 PRECOMPILED_AOT
//         })
//         private String mode;

//         @Setup(Level.Trial)
//         public void doSetup() {
//             // create the module and instantiate (the module) and connect our imports
//             wasi = WasiPreview1.builder().build();
//             var imports = ImportValues.builder().addFunction(wasi.toHostFunctions()).build();
//             InputStream wasmFileStream = ChicoryHelloTest.class.getResourceAsStream(HelloTestParams.WASM_FILENAME);
//             switch (mode) {
// //                case INTERPRETER:
// //                    instance = Instance.builder(Parser.parse(wasmFileStream))
// //                            .withImportValues(imports)
// //                            .build();
// //                    break;
//                 case RUNTIME_AOT:
//                     instance = Instance.builder(Parser.parse(wasmFileStream))
//                             .withMachineFactory(AotMachine::new)
//                             .withImportValues(imports)
//                             .build();
//                     break;
//                 case PRECOMPILED_AOT:
//                     instance = Instance.builder(HelloModule.load())
//                             .withMachineFactory(HelloModule::create)
//                             .withImportValues(imports)
//                             .build();
//                     break;
//                 default:
//                     throw new IllegalArgumentException("Unknown Chicory mode " + mode);
//             }
//         }

//         @TearDown(Level.Trial)
//         public void close() { wasi.close(); }
//     }

//     @Benchmark
//     /*
//      * Tests a simple HelloWorld Go function, including all of the calls necessary to obtain
//      * required functions.
//      */    
//     public void chicoryTest(ChicoryFixture fixture, Blackhole blackhole) throws IOException {
//         // automatically exported by TinyGo
//         final ExportFunction malloc = fixture.instance.export("malloc");
//         final ExportFunction free = fixture.instance.export("free");
//         final ExportFunction wasmFunc = fixture.instance.export(HelloTestParams.WASM_FUNCTION);
//         final Memory memory = fixture.instance.memory();

//         // allocate {fixture.paramLen} bytes of memory, this returns a pointer to that memory
//         final int ptr = (int) malloc.apply(HelloTestParams.paramLen)[0];
//         // We can now write the message to the module's memory:
//         memory.writeString(ptr, HelloTestParams.TEST_PARAM);

//         // Call the wasm function
//         final int result = (int) wasmFunc.apply(ptr, HelloTestParams.paramLen)[0];
//         // free input string memory
//         free.apply(ptr);

//         // Extract position and size from the result
//         final int valuePosition = (int) ((result >>> 32) & 0xFFFFFFFFL);
//         final int valueSize = (int) (result & 0xFFFFFFFFL);

//         // get byte[] of the result
//         byte[] message = memory.readBytes(valuePosition, valueSize);

//         blackhole.consume(message);
//     }
// }
