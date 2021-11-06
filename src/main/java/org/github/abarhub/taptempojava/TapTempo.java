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

    private static Parameter parserArguments(String[] args) {
        int precision = 0;
        int resetTime = 5;
        int sampleSize = 5;
        Options options = new Options();

        Option optHelp = new Option("h", "help", false, "Display this help message.");
        optHelp.setRequired(false);
        options.addOption(optHelp);

        Option optPrecision = new Option("p", "precision", true, "Set the decimal precision of the tempo display. Default is 0 digits, max is 5 digits.");
        optPrecision.setRequired(false);
        options.addOption(optPrecision);

        Option optResetTime = new Option("r", "reset-time", true, "Set the time in second to reset the computation. Default is 5 seconds.");
        optResetTime.setRequired(false);
        options.addOption(optResetTime);

        Option optSampleSize = new Option("s", "sample-size", true, "Set the number of samples needed to compute the tempo. Default is 5 samples.");
        optSampleSize.setRequired(false);
        options.addOption(optSampleSize);

        Option optVersion = new Option("v", "version", false, "Display the version.");
        optVersion.setRequired(false);
        options.addOption(optVersion);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
            if (cmd.hasOption('p')) {
                precision = Integer.parseInt(cmd.getOptionValue('p'));
                if (precision < 0) {
                    precision = 0;
                } else if (precision > 5) {
                    precision = 5;
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
        } catch (ParseException e) {
            System.out.println(e.getClass() + ": " + e.getMessage());
            formatter.printHelp("TempoTap", options);
            System.exit(1);
        }

        if (cmd.hasOption('h') || cmd.hasOption('v')) {
            if (cmd.hasOption('h')) {
                formatter.printHelp("TempoTap", options);
            }
            if (cmd.hasOption('v')) {
                System.out.println("Version: 1.0.0");
            }
            System.exit(0);
        }

        return new Parameter(precision, resetTime, sampleSize);
    }

    private static double computeBPM(long currentTime, long lastTime, int occurenceCount) {
        if (occurenceCount == 0) {
            occurenceCount = 1;
        }

        double elapsedTime = currentTime - lastTime;
        double meanTime = elapsedTime / occurenceCount;

        return 60.0 * 1000 / meanTime;
    }

    public static void main(String[] args) throws Exception {

        Deque<Long> hitTimePoints = new ArrayDeque<>();
        Parameter parameter = parserArguments(args);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(parameter.precision);
        df.setMinimumFractionDigits(parameter.precision);

        System.out.println("""
                Hit enter key for each beat (q to quit).
                """);

        Scanner keyboard = new Scanner(System.in);
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
                long currentTime = System.currentTimeMillis();

                // Reset if the hit diff is too big.
                if (!hitTimePoints.isEmpty() && currentTime - hitTimePoints.getLast() > parameter.resetTime * 1000L) {
                    // Clear the history.
                    hitTimePoints.clear();
                }

                hitTimePoints.add(currentTime);
                if (hitTimePoints.size() > 1) {
                    double bpm = computeBPM(hitTimePoints.getLast(), hitTimePoints.getFirst(), hitTimePoints.size() - 1);

                    String bpmRepresentation = df.format(bpm);
                    System.out.println("Tempo: " + bpmRepresentation + " bpm");
                } else {
                    System.out.println("[Hit enter key one more time to start bpm computation...]");
                }

                while (hitTimePoints.size() > parameter.sampleSize) {
                    hitTimePoints.pop();
                }
            }
        }
    }


}
