package org.example.todo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

public class TodoData {

    private static TodoData instance=new TodoData();
    private static String fileName="frifile.txt";
    private ObservableList<TodoItem> todoItems;
    private DateTimeFormatter dateTimeFormatter;


    private TodoData(){
        dateTimeFormatter=DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public void loadTodoItems() throws IOException {
        todoItems= FXCollections.observableArrayList();
        Path path= Paths.get(fileName);
        BufferedReader br= Files.newBufferedReader(path);

        String input;

        try {
            while ((input= br.readLine())!=null){
                String[] itemPieces=input.split("\t");
                String shortDescription=itemPieces[0];
                String details=itemPieces[1];
                String date=itemPieces[2];

                LocalDate localDate=LocalDate.parse(date, dateTimeFormatter);
                TodoItem item=new TodoItem(shortDescription,details,localDate);
                todoItems.add(item);
            }

        }finally {
            if (br != null){
                br.close();
            }
        }
    }

    public void storeTodoItems() throws IOException {
        Path path= Paths.get(fileName);
        BufferedWriter bw= Files.newBufferedWriter(path);

        try {
            Iterator<TodoItem> iter=todoItems.iterator();
            while (iter.hasNext()){
                TodoItem item=iter.next();
                bw.write(String.format("%s\t%s\t%s"
                        ,item.getShortDescription(), item.getDetails(),item.getDeadLine().format(dateTimeFormatter)));
                bw.newLine();
            }

        }finally {
            if (bw != null){
                bw.close();
            }
        }
    }


        public static TodoData getInstance() {
        return instance;
    }

    public ObservableList<TodoItem> getTodoItems() {
        return todoItems;
    }

    public void addTodoItem(TodoItem todoItem) {
        todoItems.add(todoItem);
    }

    public void deleteTodoItem(TodoItem item) {
        todoItems.remove(item);
    }

   /* public void setTodoItems(List<TodoItem> todoItems) {
        this.todoItems = todoItems;
    }
*/

}
