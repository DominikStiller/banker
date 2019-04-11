# Final Project for CSCI 370
This is my final project for the CSCI 370 course at Vancouver Island University.
For more information about the project, see the [Project Report](Project_Report.pdf).

Assignment: <http://csci.viu.ca/~liuh/370/project.html>

## Prerequisites

- MySQL
- Java 8

## Setup and execution
1. Create and populate the MySQL tables using the scripts inside the `sql` folder.
2. Rename `banker-example.properties` to `banker.properties` and adjust values to your environment.
3. Compile the Java application.  
   Option A: Open the IntelliJ project and use the integrated build tool.  
   Option B: Run `javac -d out -sourcepath src src/de/domistiller/banker/Main.java` from the project root.
4. Run the Java Application.  
   Option A: Run the IntelliJ project.  
   Option B: Run `java -cp out:lib/mysql-connector-java-8.0.15.jar de.domistiller.banker.Main` from the project root.
