package principal;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	public static class Count  {
		int count = 0;

		public synchronized void increment() {
			count++;
		}

		public int getCount() {
			return count;
		}
	}
	
	public static class AtomicCount  {
		AtomicInteger count = new AtomicInteger(0);

		public void increment() {
			count.getAndIncrement();
		}

		public int getCount() {
			return count.get();
		}
	}
	
	public static void main(String[] args) throws Exception {
		final int sleepThreadInterval = 2000;
		var plataformThread = getPlataformThread(sleepThreadInterval, "sample01");
		System.out.println("To join with thread sample01");
		plataformThread.join(); // join thread to main thread
		System.out.println("After join with thread sample01");

		var VirtualThread = getPlataformThread(sleepThreadInterval, "sample02");
		System.out.println("To join with thread sample02");
		VirtualThread.join(); // join thread to main thread
		System.out.println("After join with thread sample02");

		System.out.println("To get result from completableFuture: sample03");
		Future<String> completableFuture = getCompleteFutureAsync(sleepThreadInterval, "sample03");
		System.out.println("Returned from complete future: sample03: " + completableFuture.get());
		if (completableFuture.isDone())
			System.out.println(completableFuture.toString());

		System.out.println("To get result from completableFuture: sample04");
		Future<String> completableFutureLambida = getCompleteFutureLambidaAsync(sleepThreadInterval, "sample04");
		System.out.println("Returned from complete future: sample04: " + completableFutureLambida.get());

		System.out.println("To get result from completableFuture: sample05");
		getCompleteFutureLambidaCombinedAsync(sleepThreadInterval, "sample05");
		System.out.println("Returned from complete future: sample05 ");

		System.out.println("To get result from completableFuture: sample06");
		var completableFutureLambida2 = getCompletableFutureLambida2(sleepThreadInterval, "sample06");
		completableFutureLambida2.get();
		System.out.println("Returned from complete future: sample06 ");

		System.out.println("To get result from completableFuture: sample07");
		simpleThread(sleepThreadInterval, "sample07");
		System.out.println("Returned from complete future: sample07 ");

		System.out.println("To get result from sincronized count: sample08");
		getSincronizedCount();
		System.out.println("Returned from sincronizedCount: sample08");

		System.out.println("To get result from sincronized count: sample09");
		getSincronizedCount();
		System.out.println("Returned from sincronizedCount: sample09");
	
		ThreadLocal<Integer> threadLocalValue = new ThreadLocal<>();
		threadLocalValue.set(1);
		Integer result = threadLocalValue.get();
		
	
		
		System.out.println("Finished.");
	}

	private static Thread getPlataformThread(int sleepThreadInterval, String text) {
		var plataformThread = Thread.ofPlatform().unstarted(() -> {
			try {
				System.out.println("Started plataform thread: " + text);
				Thread.sleep(sleepThreadInterval);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				System.out.println("Ending plataform thread: " + text);
			}

		});
		plataformThread.start();
		return plataformThread;
	}

	private static Thread getVirtualThread(int sleepThreadInterval, String text) {
		var virtualThread = Thread.ofVirtual().unstarted(() -> {
			try {
				System.out.println("Started virtual thread:" + text);
				Thread.sleep(sleepThreadInterval);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				System.out.println("Ending virtual thread: " + text);
			}

		});
		virtualThread.start();
		return virtualThread;
	}

	private static Future<String> getCompleteFutureAsync(int sleepThreadInterval, String text)
			throws InterruptedException {

		System.out.println("Started complete future thread: " + text);
		CompletableFuture<String> completableFuture = new CompletableFuture<>();

		ExecutorService executor = Executors.newCachedThreadPool();
		executor.submit(() -> {
			try {
				Thread.sleep(sleepThreadInterval);
				System.out.println("Ending complete future thread: " + text);
				completableFuture.complete("Finished: " + text);
			} catch (InterruptedException e) {
				completableFuture.completeExceptionally(e);
			}
			return null;
		});
		executor.shutdown(); // Fecha o executor após submissão

		return completableFuture;
	}

	private static Future<String> getCompleteFutureLambidaAsync(int sleepThreadInterval, String text)
			throws InterruptedException {

		System.out.println("Started complete future lambida thread: " + text);
		CompletableFuture<String> completableFuture = CompletableFuture
				.supplyAsync(() -> "Ending complete future thread: " + text);

		return completableFuture;
	}

	private static void getCompleteFutureLambidaCombinedAsync(int sleepThreadInterval, String text)
			throws InterruptedException, ExecutionException {

		System.out.println("Started complete future lambida thread: " + text);

		ExecutorService executor = Executors.newCachedThreadPool();

		try {
			CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
				System.out.println("Processing in thread: " + Thread.currentThread().getName());
				return "Ending complete future thread: " + text;
			}, executor);

			CompletableFuture<Void> future = completableFuture.thenAcceptAsync(s -> {
				try {
					Thread.sleep(sleepThreadInterval);
					System.out.println("Computation returned: " + s);
				} catch (InterruptedException e) {
					throw new CompletionException(e);
				}
			}, executor);

			future.get();
		} finally {
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		}
	}

	private static CompletableFuture<Void> getCompletableFutureLambida2(int sleepThreadInterval, String text) {
		return CompletableFuture.runAsync(() -> {
			try {
				System.out.println("Started complete future lambida thread: " + text);
				Thread.sleep(sleepThreadInterval);
				System.out.println("Finished complete future lambida thread: " + text);
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}
		});
	}

	private static void simpleThread(int sleepThreadInterval, String text) {
		var thread = new Thread(() -> {
			System.out.println("Started simple thread: " + text);
			try {
				Thread.sleep(sleepThreadInterval);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println("Finished simple thread: " + text);
		});
		try {
			thread.start();
			thread.join();
		} catch (Exception e2) {
			throw new RuntimeException(e2);
		}
	}

	private static void getSincronizedCount() throws InterruptedException {
		final int counterLimit = 1000;
		var counter = new Count();
		var thread1 = Thread.ofVirtual().unstarted(()->{
			for (int i = 0; i < counterLimit; i++) {
				counter.increment();
			}
		});
		var thread2 = Thread.ofVirtual().unstarted(()->{
			for (int i = 0; i < counterLimit; i++) {
				counter.increment();
			}
		});
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		System.out.println(counter.getCount());
	}
	
	private static void getAtomicCount() throws InterruptedException {
		final int counterLimit = 1000;
		var counter = new AtomicCount();
		var thread1 = Thread.ofVirtual().unstarted(()->{
			for (int i = 0; i < counterLimit; i++) {
				counter.increment();
			}
		});
		var thread2 = Thread.ofVirtual().unstarted(()->{
			for (int i = 0; i < counterLimit; i++) {
				counter.increment();
			}
		});
		thread1.start();
		thread2.start();
		thread1.join();
		thread2.join();
		System.out.println(counter.getCount());
	}
}
