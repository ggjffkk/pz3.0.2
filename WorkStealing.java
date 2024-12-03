import java.io.*;
import java.nio.file.*;
import java.util.concurrent.*;
import java.util.*;

public class WorkStealing {
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

        // Пошук за допомогою Work Stealing
        long startTime = System.currentTimeMillis();
        int result = searchFilesWithWorkStealing(directoryPath, fileExtension);
        long endTime = System.currentTimeMillis() - startTime;
        System.out.println("Result Work Stealing: " + result + " files found. Time: " + endTime + " ms");
    }

    // Пошук файлів за допомогою Fork/Join (Work Stealing)
    private static int searchFilesWithWorkStealing(String directoryPath, String fileExtension) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new FileSearchTask(directoryPath, fileExtension));
    }

    // Завдання для Fork/Join (Work Stealing)
    static class FileSearchTask extends RecursiveTask<Integer> {
        private String directoryPath;
        private String fileExtension;

        public FileSearchTask(String directoryPath, String fileExtension) {
            this.directoryPath = directoryPath;
            this.fileExtension = fileExtension;
        }

        @Override
        protected Integer compute() {
            File dir = new File(directoryPath);
            File[] files = dir.listFiles();
            int count = 0;

            if (files != null) {
                List<FileSearchTask> tasks = new ArrayList<>();

                // Розподіляємо файли на підзадачі
                for (File file : files) {
                    if (file.isDirectory()) {
                        tasks.add(new FileSearchTask(file.getPath(), fileExtension));
                    } else {
                        if (file.getName().endsWith(fileExtension)) {
                            count++;
                        }
                    }
                }

                // Якщо є підзадачі, запускаємо їх
                if (!tasks.isEmpty()) {
                    for (FileSearchTask task : tasks) {
                        task.fork();
                    }
                    // Збираємо результати
                    for (FileSearchTask task : tasks) {
                        count += task.join();
                    }
                }
            }
            return count;
        }
    }
}
