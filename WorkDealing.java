import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.*;

public class WorkDealing {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Користувацьке введення
        System.out.print("Enter a directory to search for files: ");
        String directoryPath = scanner.nextLine();
        System.out.print("Enter a file extension (for example, .pdf): ");
        String fileExtension = scanner.nextLine();

        // Визначаємо, чи існує така директорія
        File dir = new File(directoryPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("The specified directory does not exist or is not a directory.");
            return;
        }

        // Пошук за допомогою Work Dealing
        long startTime = System.currentTimeMillis();
        int result = searchFilesWithWorkDealing(directoryPath, fileExtension);
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Result Work Dealing: " + result + " files found. Time: " + endTime + " ms");
    }

    // Пошук файлів за допомогою ExecutorService (Work Dealing)
    private static int searchFilesWithWorkDealing(String directoryPath, String fileExtension) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Integer>> futures = new ArrayList<>();
        File dir = new File(directoryPath);
        File[] files = dir.listFiles();
        int chunkSize = files.length / Runtime.getRuntime().availableProcessors();

        // Розподіляємо роботу між потоками
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            int start = i * chunkSize;
            int end = (i == Runtime.getRuntime().availableProcessors() - 1) ? files.length : (i + 1) * chunkSize;
            futures.add(executor.submit(new FileSearchCallable(Arrays.copyOfRange(files, start, end), fileExtension)));
        }

        // Обчислюємо результат
        int totalCount = 0;
        for (Future<Integer> future : futures) {
            try {
                totalCount += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown();
        return totalCount;
    }

    // Завдання для ExecutorService (Work Dealing)
    static class FileSearchCallable implements Callable<Integer> {
        private File[] files;
        private String fileExtension;

        public FileSearchCallable(File[] files, String fileExtension) {
            this.files = files;
            this.fileExtension = fileExtension;
        }

        @Override
        public Integer call() {
            int count = 0;
            for (File file : files) {
                if (file.isDirectory()) {
                    // Пропускаємо директорії
                    continue;
                }
                if (file.getName().endsWith(fileExtension)) {
                    count++;
                }
            }
            return count;
        }
    }
}
