package org.devinebyte.compiler.cli.options;
import org.devinebyte.sdk.diagnostics.DiagnosticSeverity;

import java.nio.file.Path;

public class OptionParser {

    public CliOptions parse(String[] args) {

        CliOptions options = new CliOptions();

        for (int i = 0; i < args.length; i++) {

            String arg = args[i];

            switch (arg) {

                case "-i":
                case "--input":
                    options.setInput(Path.of(requireValue(args, ++i, arg)));
                    break;

                case "-o":
                case "--output":
                    options.setOutput(Path.of(requireValue(args, ++i, arg)));
                    break;

                case "--incremental":
                    options.setIncremental(true);
                    break;

                case "--optimize":
                    options.setOptimize(true);
                    break;

                case "--strict":
                    options.setStrict(true);
                    break;

                case "-v":
                case "--verbose":
                    options.setVerbose(true);
                    break;

                default:
                    throw new OptionException("Unknown option: " + arg);
            }
        }

        return options;
    }

    private String requireValue(String[] args, int index, String option) {

        if (index >= args.length) {
            throw new OptionException("Missing value for " + option);
        }

        return args[index];
    }
}
