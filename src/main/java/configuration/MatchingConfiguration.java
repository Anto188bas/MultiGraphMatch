package configuration;

public class MatchingConfiguration {

    public String targetDirectory;
    public String queriesDirectory;
    public String resultsFile;
    public String outFile;
    public int timeout;

    public MatchingConfiguration(String[] args) {
        timeout = 1800;
        outFile = null;
        resultsFile = null;
        for (int i = 0; i < args.length; i++)
            readParameter(args[i], args[++i]);
        validateParameters();
    }

    public void readParameter(String key, String value) {
        switch (key) {
            case "-g" -> targetDirectory  = value;
            case "-q" -> queriesDirectory = value;
            case "-r" -> resultsFile      = value;
            case "-o" -> outFile          = value;
            case "-t" -> timeout          = Integer.parseInt(value);
            default -> printHelp(key);
        }
    }

    private void printHelp(String parameter) {
        String help = "Usage: java -cp GraphMatchingRI.jar TestMatching -g <targetGraphFolder> -q <queriesFolder> -o <outFile> -r <resultsFile>\n\n";
        help += "REQUIRED PARAMETERS:\n";
        help += "-g\tTarget graph directory\n";
        help += "-q\tQueries directory\n";
        if (parameter != null) System.out.println("Error! Unrecognizable command '" + parameter + "'");
        System.out.println(help);
    }

    private void printError(String parameter) {
        if (parameter != null) return;
        System.out.println("Error!");
        printHelp(null);
        System.exit(1);
    }

    private void validateParameters() {
        printError(targetDirectory);
        printError(queriesDirectory);
    }
}
