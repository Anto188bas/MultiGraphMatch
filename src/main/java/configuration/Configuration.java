package configuration;

import java.io.PrintStream;

public class Configuration {
    public String nodes_main_directory;
    public String edges_main_directory;
    public String query_file;
    public String result_file;
    public int    timeout;

    public Configuration() {}
    public Configuration(String[] args){
        set_error();
        timeout = 1800;
        for(int i = 0; i < args.length; i++)
            option_setting(args[i], args[++i]);
        validate_input_parameter();
    }

    public void option_setting(String key, String value){
        switch (key){
            case "-n"   -> nodes_main_directory = value;
            case "-e"   -> edges_main_directory = value;
            case "-q"   -> query_file           = value;
            case "-r"   -> result_file          = value;
            case "-t"   -> timeout              = Integer.parseInt(value);
            default     -> printHelp(key);
        }
    }

    private void printHelp(String parameter)
    {
        String help = "Usage: java -jar MultiRI.jar -n <nodeFolder> -e <edgeFolder> -q <queryFile> -r <resultsFile>\n\n";
        help+="REQUIRED PARAMETERS:\n";
        help+="-n\tTarget nodes folder\n";
        help+="-e\tTarget edges folder\n";
        help+="-q\tQueries file\n";
        help+="-r\tResults file\n\n";
        help+="-u\tUse uncompressed data structure to store whole target\n\n";
        if(parameter != null)
            System.out.println("Error! Unrecognizable command '" + parameter + "'");
        System.out.println(help);
    }

    private void printError(String parameter){
        if(parameter != null) return;
        System.out.println("Error! No path for target nodes has been specified!\n");
        printHelp(null);
        System.exit(1);
    }

    private void validate_input_parameter(){
        printError(nodes_main_directory);
        printError(edges_main_directory);
        // TODO add all checks
    }

    private void set_error(){
        System.setErr(new PrintStream(System.err) {
            public void println(String l) {
                if (!l.startsWith("SLF4J")) {
                    super.println(l);
                }
            }
        });
    }
}
