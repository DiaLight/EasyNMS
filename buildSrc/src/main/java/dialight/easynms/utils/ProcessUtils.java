package dialight.easynms.utils;

import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessUtils {


    private static final Path JAVA_EXE = Paths.get(System.getProperty("java.home"), "bin", "java");

    public static Result read(Process proc) throws IOException, InterruptedException {
        List<String> lines = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader ibr = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            try(BufferedReader ebr = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                String eline = "";
                String line = "";
                while(proc.isAlive()) {
                    if(ebr.ready()) {
                        eline = ebr.readLine();
                        if(eline != null) {
                            errors.add(eline);
                        }
                    } else {
                        eline = "";
                    }
                    if(ibr.ready()) {
                        line = ibr.readLine();
                        if(line != null) {
                            lines.add(line);
                        }
                    } else {
                        line = "";
                    }
                    if((eline == null || eline.isEmpty()) && (line == null || line.isEmpty())) {
                        Thread.sleep(100);
                    }
                }
                ibr.lines().forEach(lines::add);
                ebr.lines().forEach(errors::add);
            }
        }
        int exitCode = proc.waitFor();
        return new Result(lines, errors, exitCode);
    }
    public static void run(@Nullable Path wd, String... args) {
//        System.out.println(String.join(" ", Arrays.asList(args)));
        try {
            final var builder = new ProcessBuilder();
            builder.command(args);
            if(wd != null) builder.directory(wd.toFile());
            Process proc = builder.start();
            Result result = read(proc);
            if(result.exitCode != 0) {
                for (String line : result.stdout) System.out.println(line);
                for (String error : result.stderr) System.err.println(error);
                throw new RuntimeException(Arrays.toString(args) + " bad exit code " + result.exitCode);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    public static class Result {
        public final List<String> stdout;
        public final List<String> stderr;
        public final int exitCode;

        public Result(List<String> stdout, List<String> stderr, int exitCode) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
        }

    }

    public static void handleIO(Process proc) throws IOException, InterruptedException {
        List<String> lines = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        try(BufferedReader ibr = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
            try(BufferedReader ebr = new BufferedReader(new InputStreamReader(proc.getErrorStream()))) {
                boolean action = false;
                String line;
                while(proc.isAlive()) {
                    if(ebr.ready() && (line = ebr.readLine()) != null) {
//                        errors.add(line);
                        System.err.println(line);
                        action = true;
                    }
                    if(ibr.ready() && (line = ibr.readLine()) != null) {
//                        lines.add(line);
                        System.out.println(line);
                        action = true;
                    }
                    if(!action) Thread.sleep(100);
                    else action = false;
                }
                ibr.lines().forEach(lines::add);
                ebr.lines().forEach(errors::add);
            }
        }
        int exitCode = proc.waitFor();
        if(exitCode != 0) {
            for (String line : lines) System.out.println(line);
            for (String error : errors) System.err.println(error);
            throw new IllegalStateException("Bad exit code " + exitCode);
        }
    }

}
