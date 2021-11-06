package org.github.abarhub.taptempojava;

import org.apache.commons.cli.*;

import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;

public class TapTempo {

    record Parameter(int precision, int resetTime, int sampleSize) {
    }

    enum Action {
        END, CALCULATE, OTHER
    }

    static class ExitException extends RuntimeException {
        private final int exitCode;

        public ExitException(int exitCode) {
            this.exitCode = exitCode;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    private static Parameter parserArguments(String[] args) {
        var precision = 0;
        var precisionMax = 5;
        var resetTime = 5;
        var sampleSize = 5;
        Options options = new Options();

        var optHelp = new Option("h", "help", false,
                "Display this help message.");
        optHelp.setRequired(false);
        options.addOption(optHelp);

        var optPrecision = new Option("p", "precision", true, """
                Set the decimal precision of the tempo display. \
                Default is %d digits, max is %d digits.""".formatted(precision, precisionMax));
        optPrecision.setRequired(false);
        options.addOption(optPrecision);

        var optResetTime = new Option("r", "reset-time", true, """
                Set the time in second to reset the computation. \
                Default is %d seconds.""".formatted(resetTime));
        optResetTime.setRequired(false);
        options.addOption(optResetTime);

        var optSampleSize = new Option("s", "sample-size", true, """
                Set the number of samples needed to compute the tempo. \
                Default is %d samples.""".formatted(sampleSize));
        optSampleSize.setRequired(false);
        options.addOption(optSampleSize);

        var optVersion = new Option("v", "version", false,
                "Display the version.");
        optVersion.setRequired(false);
        options.addOption(optVersion);

        var parser = new DefaultParser();
        var formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption('p')) {
                precision = Integer.parseInt(cmd.getOptionValue('p'));
                if (precision < 0) {
                    precision = 0;
                } else if (precision > precisionMax) {
                    precision = precisionMax;
                }
            }
            if (cmd.hasOption('r')) {
                resetTime = Integer.parseInt(cmd.getOptionValue('r'));
                if (resetTime < 1) {
                    resetTime = 1;
                }
            }
            if (cmd.hasOption('s')) {
                sampleSize = Integer.parseInt(cmd.getOptionValue('s'));
                if (sampleSize < 1) {
                    sampleSize = 1;
                }
            }
        } catch (NumberFormatException | ParseException e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
            formatter.printHelp("TempoTap", options);
            throw new ExitException(1);
        }

        if (cmd.hasOption('h') || cmd.hasOption('v')) {
            if (cmd.hasOption('h')) {
                formatter.printHelp("TempoTap", options);
            }
            if (cmd.hasOption('v')) {
                System.out.println("Version: 1.0.0");
            }
            throw new ExitException(0);
        }

        return new Parameter(precision, resetTime, sampleSize);
    }

    private static double computeBPM(long currentTime, long lastTime, int occurenceCount) {
        if (occurenceCount == 0) {
            occurenceCount = 1;
        }

        double elapsedTime = currentTime - lastTime;
        var meanTime = elapsedTime / occurenceCount;

        return 60.0 * 1000 / meanTime;
    }

    public static void run(String[] args) {

        Deque<Long> hitTimePoints = new ArrayDeque<>();
        var parameter = parserArguments(args);

        var df = new DecimalFormat();
        df.setMaximumFractionDigits(parameter.precision);
        df.setMinimumFractionDigits(parameter.precision);

        System.out.println("""
                Hit enter key for each beat (q to quit).
                """);

        var keyboard = new Scanner(System.in);
        keyboard.useDelimiter("");

        boolean shouldContinue = true;
        while (shouldContinue) {

            Action action;
            do {
                char c = keyboard.next().charAt(0);
                action = switch (c) {
                    case 'q' -> Action.END;
                    case '\n' -> Action.CALCULATE;
                    default -> Action.OTHER;

                };
            } while (action == Action.OTHER);

            if (action == Action.END) {
                shouldContinue = false;
                System.out.println("""
                        Bye Bye!
                        """);
            } else {
                var currentTime = System.currentTimeMillis();

                // Reset if the hit diff is too big.
                if (!hitTimePoints.isEmpty() && currentTime - hitTimePoints.getLast() > parameter.resetTime * 1000L) {
                    // Clear the history.
                    hitTimePoints.clear();
                }

                hitTimePoints.add(currentTime);
                if (hitTimePoints.size() > 1) {
                    double bpm = computeBPM(hitTimePoints.getLast(), hitTimePoints.getFirst(), hitTimePoints.size() - 1);

                    String bpmRepresentation = df.format(bpm);
                    System.out.printf("Tempo: %s bpm%n", bpmRepresentation);
                } else {
                    System.out.println("[Hit enter key one more time to start bpm computation...]");
                }

                while (hitTimePoints.size() > parameter.sampleSize) {
                    hitTimePoints.pop();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        run(args);
    }

}
