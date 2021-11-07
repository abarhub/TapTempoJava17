package org.github.abarhub.taptempojava;

import org.apache.commons.cli.*;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.ResourceBundle;
import java.util.Scanner;

public class TapTempo {

    private static Clock clock = Clock.systemUTC();

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

    private static Parameter parserArguments(String[] args, ResourceBundle messages) {
        var precision = 0;
        var precisionMax = 5;
        var resetTime = 5;
        var sampleSize = 5;
        var options = new Options();

        var optHelp = new Option("h", "help", false,
                messages.getString("cliHelp"));
        optHelp.setRequired(false);
        options.addOption(optHelp);

        var optPrecision = new Option("p", "precision", true,
                messages.getString("cliPrecision").formatted(precision, precisionMax));
        optPrecision.setRequired(false);
        options.addOption(optPrecision);

        var optResetTime = new Option("r", "reset-time", true,
                messages.getString("cliReset").formatted(resetTime));
        optResetTime.setRequired(false);
        options.addOption(optResetTime);

        var optSampleSize = new Option("s", "sample-size", true,
                messages.getString("cliNbSample").formatted(sampleSize));
        optSampleSize.setRequired(false);
        options.addOption(optSampleSize);

        var optVersion = new Option("v", "version", false,
                messages.getString("cliVersion"));
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
                System.out.printf((messages.getString("version")), Version.getVersion());
            }
            throw new ExitException(0);
        }

        return new Parameter(precision, resetTime, sampleSize);
    }

    public static double computeBPM(Instant currentTime, Instant lastTime, int occurenceCount) {
        if (occurenceCount == 0) {
            occurenceCount = 1;
        }

        Duration elapsedTime = Duration.between(lastTime, currentTime);
        var meanTime = elapsedTime.dividedBy(occurenceCount);

        return 60.0 * 1000 / meanTime.toMillis();
    }

    public static boolean compareDiff(Instant lastTime, Instant currentTime, long resetTime) {
        return Duration.between(lastTime, currentTime).compareTo(Duration.ofSeconds(resetTime)) > 0;
    }

    public static void setClock(Clock clock) {
        TapTempo.clock = clock;
    }

    public static void run(String[] args) {

        ResourceBundle messages = ResourceBundle.getBundle("Message");
        Deque<Instant> hitTimePoints = new ArrayDeque<>();
        var parameter = parserArguments(args, messages);

        var df = new DecimalFormat();
        df.setMaximumFractionDigits(parameter.precision);
        df.setMinimumFractionDigits(parameter.precision);

        System.out.println(messages.getString("start"));

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
                System.out.println(messages.getString("quit"));
            } else {
                var currentTime = Instant.now(clock);


                // Reset if the hit diff is too big.
                if (!hitTimePoints.isEmpty() && compareDiff(hitTimePoints.getLast(), currentTime, parameter.resetTime)) {
                    // Clear the history.
                    hitTimePoints.clear();
                }

                hitTimePoints.add(currentTime);
                if (hitTimePoints.size() > 1) {
                    var bpm = computeBPM(hitTimePoints.getLast(), hitTimePoints.getFirst(), hitTimePoints.size() - 1);

                    String bpmRepresentation = df.format(bpm);
                    System.out.printf(messages.getString("tempo"), bpmRepresentation);
                } else {
                    System.out.println(messages.getString("hitEnter"));
                }

                while (hitTimePoints.size() > parameter.sampleSize) {
                    hitTimePoints.pop();
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            run(args);
        } catch (ExitException e) {
            System.exit(e.exitCode);
        }
    }

}
