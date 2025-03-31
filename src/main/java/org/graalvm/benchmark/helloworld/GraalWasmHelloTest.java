// /*
//  * Copyright (c) 2024, Oracle and/or its affiliates.
//  *
//  * Licensed under the Universal Permissive License v 1.0 as shown at https://opensource.org/license/UPL.
//  */

//  package org.graalvm.benchmark.helloworld;

// import java.io.IOException;
// import java.util.concurrent.TimeUnit;

// import org.graalvm.polyglot.Context;
// import org.graalvm.polyglot.Source;
// import org.graalvm.polyglot.Value;
// import org.openjdk.jmh.annotations.Benchmark;
// import org.openjdk.jmh.annotations.BenchmarkMode;
// import org.openjdk.jmh.annotations.Fork;
// import org.openjdk.jmh.annotations.Level;
// import org.openjdk.jmh.annotations.Measurement;
// import org.openjdk.jmh.annotations.Mode;
// import org.openjdk.jmh.annotations.OutputTimeUnit;
// import org.openjdk.jmh.annotations.Scope;
// import org.openjdk.jmh.annotations.Setup;
// import org.openjdk.jmh.annotations.State;
// import org.openjdk.jmh.annotations.TearDown;
// import org.openjdk.jmh.annotations.Warmup;
// import org.openjdk.jmh.infra.Blackhole;


// @Warmup(iterations = 3)
// @Measurement(iterations = 3)
// @BenchmarkMode(Mode.Throughput)
// @OutputTimeUnit(TimeUnit.SECONDS)
// @Fork(1)
// public class GraalWasmHelloTest {

//     @State(Scope.Benchmark)
//     public static class GraalWasmFixture {
//         public Value instance;
//         public Context context;

//         @Setup(Level.Trial)
//         public void doSetup() throws IOException {
//             context = Context.newBuilder("wasm")
//                     .option("wasm.Builtins", "wasi_snapshot_preview1")
//                     .build();
//             final Source source = Source.newBuilder("wasm", GraalWasmFixture.class.getResource(HelloTestParams.WASM_FILENAME))
//                 .name("hello")
//                 .build();          
//             context.eval(source);
//             instance = context.getBindings("wasm").getMember("hello");
//         }

//         @TearDown(Level.Trial)
//         public void doTeardown() {
//             context.close();
//         }
//     }

//     @Benchmark
//     /*
//      * Tests a simple HelloWorld Go function, including all of the calls necessary to obtain
//      * required functions.
//      */
//     public void graalWasmTest(GraalWasmFixture fixture, Blackhole blackhole) throws IOException {
//         // automatically exported by TinyGo
//         final Value malloc = fixture.instance.getMember("malloc");
//         final Value free = fixture.instance.getMember("free");
//         final Value wasmFunc = fixture.instance.getMember(HelloTestParams.WASM_FUNCTION);
//         final Value memory = fixture.instance.getMember("memory");

//         // allocate {fixture.paramLen} bytes of memory, this returns a pointer to that memory
//         final int ptr = malloc.execute(HelloTestParams.paramLen).asInt();
//         // write the message to the module's memory
//         writeString(memory, ptr, HelloTestParams.paramBytes);

//         // call the wasm function
//         final Value result = wasmFunc.execute(ptr, HelloTestParams.paramLen);
//         // free input string memory
//         free.executeVoid(ptr);

//         // extract position and size from the result
//         final int valuePosition = (int) ((result.asLong() >>> 32) & 0xFFFFFFFFL);
//         final int valueSize = (int) (result.asLong() & 0xFFFFFFFFL);

//         // get byte[] of the result
//         byte[] message = new byte[valueSize];
//         memory.readBuffer(valuePosition, message, 0, valueSize);

//         blackhole.consume(message);
//     }

//     private void writeString(Value memory, int ptr, byte[] data) {
//         for (int i = 0; i < data.length; i++) {
//             memory.writeBufferByte(ptr + i, data[i]);
//         }
//     }
// }