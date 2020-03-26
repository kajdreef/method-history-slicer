package org.spideruci.experiments;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spideruci.history.slicer.slicers.HistorySlicer;
import org.spideruci.history.slicer.slicers.HistorySlicerBuilder;
import org.spideruci.line.extractor.ParserLauncher;
import org.spideruci.line.extractor.parsers.components.Component;
import org.spideruci.line.extractor.parsers.components.MethodSignature;

public class Experiment1 {

    private String projectPath;
    private Repository repo;
    private Git git;
    private String pastCommit;
    private String presentCommit;
    private int epoch;
    private Logger logger = LoggerFactory.getLogger(Experiment1.class);

    public Experiment1() {

    }

    public Experiment1 setProject(String projectPath) {
        this.projectPath = projectPath;
        File repoDirectory = new File(projectPath + File.separator + ".git");

        try {
            this.repo = new FileRepositoryBuilder().setGitDir(repoDirectory).build();
            this.git = new Git(repo);
        } catch (IOException e) {

        }

        return this;
    }

    public Experiment1 setEpoch(int epoch) {
        this.epoch = epoch;
        return this;
    }

    public Experiment1 setCommitRange(String pastCommit, String presentCommit) {
        this.pastCommit = pastCommit;
        this.presentCommit = presentCommit;
        return this;
    }

    public Set<Component> intersectionList(Set<Component> set1, Set<Component> set2) {
        return set2.stream().filter(set1::contains).collect(Collectors.toSet());
    }

    public void run() throws RefAlreadyExistsException, RefNotFoundException, InvalidRefNameException,
            CheckoutConflictException, GitAPIException {
        logger.info("Config - sut: {}, past-commit: {}, present-commit: {}", this.projectPath, this.pastCommit, this.presentCommit);
        
        // Get methods that appear in both snapshots
        git.checkout().setName(this.pastCommit).call();
        Set<Component> pastMethodSet = new ParserLauncher().start(this.projectPath);
        
        git.reset().setMode(ResetType.HARD).call();

        git.checkout().setName(this.presentCommit).call();
        Set<Component> presentMethodSet = new ParserLauncher().start(this.projectPath);

        // Get the intersection and filter out all the method signtarues that contain 'test' in their filepath
        Set<Component> intersection = intersectionList(pastMethodSet, presentMethodSet).stream()
            .filter(c -> {
                if (c instanceof MethodSignature) {
                    MethodSignature m = (MethodSignature) c;
                    return ! m.file_path.contains("test");
                }
                else {
                    return false;
                }
            }).collect(Collectors.toSet());

        logger.info("past: {}, present: {}, intersection: {}", pastMethodSet.size(), presentMethodSet.size(), intersection.size());
        assert intersection.size() <= presentMethodSet.size();
        assert intersection.size() <= pastMethodSet.size();

        // Get the number of times a method was changed 
        HistorySlicer slicer = HistorySlicerBuilder.getInstance()
            .setForwardSlicing(false)
            .build(this.repo); 

        slicer.setCommitRange(this.pastCommit, this.presentCommit);

        Map<Component, List<String>> methodCommitsMap = new HashMap<>();
        for (Component c : intersection) {
            if (c instanceof MethodSignature) {
                MethodSignature m = (MethodSignature) c;
                List<String> list = slicer.trace(m.file_path, m.line_start, m.line_end);
                methodCommitsMap.put(c, list);
                // if (list.size() > 0)
                System.out.println(String.format("%s - %d", m.asString(), list.size()));
            }
        }

        git.reset().setMode(ResetType.HARD).call();
        git.checkout().setName("master").call();
    }

    public static void main(String[] args) throws ParseException {
        Options options = new Options();

        options.addOption("s", "sut", true, "Path to the system under study.");
        options.addOption("pa", "past", true, "Starting commit from which the experiment starts.");
        options.addOption("pr", "present", true, "Starting commit from which the experiment starts.");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        // Initialize the experiment and set all the parameters
        Experiment1 experiment1 = new Experiment1();

        if (cmd.hasOption("sut") && cmd.hasOption("past") && cmd.hasOption("present")) {
            experiment1.setProject(cmd.getOptionValue("sut"));
            experiment1.setCommitRange(cmd.getOptionValue("past"), cmd.getOptionValue("present"));
        }
        else {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("CLITester", options);
            System.exit(1);
        }

        // Run the experiment
        try{
            experiment1.run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}