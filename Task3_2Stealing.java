import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class Task3_2Stealing {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== File Search (Work Stealing) ===");

        // Input Directory
        File directory = getValidDirectory(scanner);

        // Input Search Term
        System.out.print("Enter letter or word to search in filenames: ");
        String searchTerm = scanner.next();

        // Execution
        ForkJoinPool pool = new ForkJoinPool();
        FileSearchTask task = new FileSearchTask(directory, searchTerm);

        long start = System.nanoTime();
        long count = pool.invoke(task);
        long end = System.nanoTime();

        // Output
        System.out.println("Total files found: " + count);
        System.out.printf("Time taken: %.3f ms%n", (end - start) / 1_000_000.0);
    }

    // Recursive Task for directory walking
    static class FileSearchTask extends RecursiveTask<Long> {
        private final File dir;
        private final String term;

        public FileSearchTask(File dir, String term) {
            this.dir = dir;
            this.term = term;
        }

        @Override
        protected Long compute() {
            long count = 0;
            List<FileSearchTask> subTasks = new ArrayList<>();

            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Fork new task for subdirectory
                        FileSearchTask subTask = new FileSearchTask(file, term);
                        subTask.fork(); // Push to deque (Stealing enabled)
                        subTasks.add(subTask);
                    } else {
                        // Check file name directly
                        if (file.getName().contains(term)) {
                            count++;
                        }
                    }
                }
            }

            // Join results from subtasks
            for (FileSearchTask task : subTasks) {
                count += task.join();
            }

            return count;
        }
    }

    // Helper: Validate Directory Input
    private static File getValidDirectory(Scanner s) {
        while (true) {
            System.out.print("Enter directory path: ");
            String path = s.next();
            File file = new File(path);
            if (file.exists() && file.isDirectory()) {
                return file;
            }
            System.out.println("Invalid path or not a directory. Try again.");
        }
    }
}