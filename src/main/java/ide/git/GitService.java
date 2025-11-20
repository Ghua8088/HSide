package ide.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class GitService {

    public static boolean isGitRepository(File dir) {
        if (dir == null) return false;
        File gitDir = new File(dir, ".git");
        if (gitDir.exists() && gitDir.isDirectory()) return true;
        // Fallback to running git command
        String out = runGit(dir, "rev-parse", "--is-inside-work-tree");
        return out != null && out.trim().equalsIgnoreCase("true");
    }

    public static String getStatus(File dir) {
        if (dir == null) return "No project directory selected.";
        if (!isGitRepository(dir)) return "Not a Git repository: " + dir.getAbsolutePath();
        String status = runGit(dir, "status", "--porcelain=1");
        if (status == null || status.isEmpty()) return "Working tree clean.";
        return status;
    }

    public static String getBranches(File dir) {
        if (dir == null) return "No project directory selected.";
        String out = runGit(dir, "branch", "--list");
        return out == null ? "(no branch info)" : out;
    }

    private static String runGit(File dir, String... args) {
        try {
            String[] cmd = new String[args.length + 1];
            cmd[0] = "git";
            System.arraycopy(args, 0, cmd, 1, args.length);

            ProcessBuilder pb = new ProcessBuilder(cmd);
            if (dir != null) pb.directory(dir);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append(System.lineSeparator());
                }
                p.waitFor();
                return sb.toString().trim();
            }
        } catch (Exception e) {
            return "Error running git: " + e.getMessage();
        }
    }
}
