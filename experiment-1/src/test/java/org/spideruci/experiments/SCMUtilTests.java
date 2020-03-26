// package org.spideruci.history.slicer.slicers;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.util.List;

// import org.eclipse.jgit.api.Git;
// import org.eclipse.jgit.api.errors.GitAPIException;
// import org.eclipse.jgit.lib.Repository;
// import org.junit.jupiter.api.Assertions;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.io.TempDir;

// class SCMUtilTests {

//     public Git git;
//     public Repository repo;

//     private Path file_1_path, file_2_path;

//     @BeforeEach
//     public void setupGitRepo(@TempDir Path rootGitRepo) {
//         // Initialze a git repo
//         try {
//             this.git = Git.init().setDirectory(rootGitRepo.toFile()).call();
//             this.repo = this.git.getRepository();

//             // Create two files in created directory
//             String contentFile1 = "public class A {\n\tpublic void methodA(){\n\t}\n}\r";
//             file_1_path = rootGitRepo.relativize(Files.write(rootGitRepo.resolve("A.java"), contentFile1.getBytes()));

//             String contentFile2 = "public class B {\n\tpublic void methodB(){\n\t}\n}\r";
//             file_2_path = rootGitRepo.relativize(Files.write(rootGitRepo.resolve("B.java"), contentFile2.getBytes()));

//             // Add file 1 to the repo and commit
//             this.git.add().addFilepattern(file_1_path.toString()).call();
//             this.git.commit().setMessage("Add file 1").call();

//             // Add a file_2 and commit
//             this.git.add().addFilepattern(file_2_path.toString()).call();
//             this.git.commit().setMessage("Add file 2").call();

//             // Modify file_1, add, and commit
//             String newContentFile1 = "public class A {\n\tpublic void methodA(int A){\n\t}\n}\r";
//             Files.write(rootGitRepo.resolve("A.java"), newContentFile1.getBytes());

//             this.git.add().addFilepattern(file_1_path.toString()).call();
//             this.git.commit().setMessage("Modified file 1").call();

//         } catch (IllegalStateException | GitAPIException | IOException e) {
//             // TODO Auto-generated catch block
//             e.printStackTrace();
//         }
//     }

//     @Test
//     public void traceNumberOfCommitsTouchedFile() {
    
//     }
// }