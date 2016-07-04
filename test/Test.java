/*
 * NOTICE
 *
 * Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
 *
 * Unless otherwise noted, all of the material in this file is Copyright (c) 2016
 * by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
 * may be reproduced, published, distributed, displayed, performed, copied,
 * stored, modified, transmitted or otherwise used or viewed by anyone other
 * than the authors (David C Vernet and Matthew J Perron),
 * for either public or private use.
 *
 * No part of this file may be modified, changed, exploited, or in any way
 * used for derivative works or offered for sale without the express
 * written permission of the authors.
 */

package test;

import java.util.concurrent.*;
import java.util.ArrayList;

public class Test {

	public static final long LONG_LOOP_ITERS = 20000000L;
	public static final long SHORT_LOOP_ITERS = 10000000L;
	public static final int numThreads = 32;
	public static ExecutorService executor = Executors
			.newFixedThreadPool(numThreads);
	public static ArrayList<Callable<Void>> threads = new ArrayList<>();

	public static void main(String[] args) throws InterruptedException {
		threads.add(new LongWorker());
		threads.add(new ShortWorker());
		
		while (true) {
			doParallel();
			System.out.println("Iteration done");
		}
	}

	public static void doParallel() throws InterruptedException {
		executor.invokeAll(threads);
	}

	static class LongWorker implements Callable<Void> {
		public Void call() {
			long sum = 0;
			for (long i = 0; i < LONG_LOOP_ITERS; i++)
				sum += (System.nanoTime() % 9999);
			System.out.println("Long done");
			return null;
		}
	}
	
	static class ShortWorker implements Callable<Void> {
		public Void call() {
			long sum = 0;
			for (long i = 0; i < SHORT_LOOP_ITERS; i++)
				sum += (System.nanoTime() % 9999);
			System.out.println("Short done");
			return null;
		}
	}

}