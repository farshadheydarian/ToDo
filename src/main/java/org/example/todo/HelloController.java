package org.example.todo;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HelloController {
    @FXML
    private List<TodoItem> todoItems;
    @FXML
    private ListView<TodoItem> todoListView;
    @FXML
    private TextArea textArea;

    @FXML
    private Label deadlineLabel;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listContextMenu;
    @FXML
    private ToggleButton filterToggleButton;
    private FilteredList<TodoItem> filteredList;
    private Predicate<TodoItem> wantAllItems;
    private Predicate<TodoItem> wantTodaysItems;

    public void initialize(){
        listContextMenu=new ContextMenu();
        MenuItem deleteMenuItem=new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TodoItem item=todoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listContextMenu.getItems().addAll(deleteMenuItem);
        todoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TodoItem>() {
            @Override
            public void changed(ObservableValue<? extends TodoItem> observableValue, TodoItem todoItem, TodoItem t1) {
                if(t1 != null) {
                    TodoItem item = todoListView.getSelectionModel().getSelectedItem();
                    textArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    System.out.println(item.getDetails());
                    System.out.println(item.getDeadLine());
                    deadlineLabel.setText(df.format(item.getDeadLine()));
                }
            }
        });

        wantAllItems=new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        };

        wantTodaysItems=new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return todoItem.getDeadLine().equals(LocalDate.now());
            }
        };
        filteredList=new FilteredList<>(TodoData.getInstance().getTodoItems(),wantAllItems);
              /*  new Predicate<TodoItem>() {
            @Override
            public boolean test(TodoItem todoItem) {
                return true;
            }
        });*/

        SortedList<TodoItem> sortedList=new SortedList<TodoItem>(filteredList , new Comparator<TodoItem>() {
            @Override
            public int compare(TodoItem o1, TodoItem o2) {
                return o1.getDeadLine().compareTo(o2.getDeadLine());
            }
        });
        //todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
       // todoListView.setItems(TodoData.getInstance().getTodoItems());
        todoListView.setItems(sortedList);
        todoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        todoListView.getSelectionModel().selectFirst();

        todoListView.setCellFactory(new Callback<ListView<TodoItem>, ListCell<TodoItem>>() {
            @Override
            public ListCell<TodoItem> call(ListView<TodoItem> todoItemListView) {
                ListCell<TodoItem> cell=new ListCell<>(){
                    @Override
                    protected void updateItem(TodoItem todoItem, boolean b) {
                        super.updateItem(todoItem, b);
                        if (b){
                            setText(null);
                        }else {
                            setText(todoItem.getShortDescription());
                            if (todoItem.getDeadLine().isBefore(LocalDate.now().plusDays(1))){
                                setTextFill(Color.RED);
                            } else if (todoItem.getDeadLine().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.GREEN);
                            }

                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs ,wasEmpty , isNowEmpty)->{

                            if(isNowEmpty){
                                cell.setContextMenu(null);
                            }else {
                                cell.setContextMenu(listContextMenu);
                            }
                        });
                return cell;
            }
        });
    }

    public void handleFilterButton(){
        TodoItem item=todoListView.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()){
            filteredList.setPredicate(wantTodaysItems);
            if (filteredList.isEmpty()){
                textArea.clear();
                deadlineLabel.setText("");
            }else if (filteredList.contains(item)){
                todoListView.getSelectionModel().select(item);
            }else {
                todoListView.getSelectionModel().selectFirst();
            }
        }else {
            filteredList.setPredicate(wantAllItems);
            todoListView.getSelectionModel().select(item);
        }
    }

    @FXML
    public void handleExit(){
        Platform.exit();
    }

    private void deleteItem(TodoItem item) {
        Alert alert=new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete todo item");
        alert.setHeaderText("Delete item : " + item.getShortDescription());
        alert.setContentText("Are you sure ? press Ok to confirm , or Cancel to back out");
        Optional<ButtonType> result=alert.showAndWait();

        if(result.isPresent() && result.get()==ButtonType.OK){
            TodoData.getInstance().deleteTodoItem(item);
        }
    }

    @FXML
    public void showNewItemDialog(){
        Dialog<ButtonType> dialog=new Dialog<>();
        dialog.setTitle("Insert");
        dialog.setHeaderText("Now insert a new item");
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        FXMLLoader loader=new FXMLLoader();
        loader.setLocation(getClass().getResource("todoItemDialog.fxml"));

        try {
           // Parent root =  FXMLLoader.load(HelloController.class.getResource("todoItemDialog.fxml"));
            dialog.getDialogPane().setContent(loader.load());
        }catch (IOException e){
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result=dialog.showAndWait();
        if (result.isPresent() && result.get()==ButtonType.OK){
            DialogController controller=loader.getController();
            TodoItem newItem = controller.processResult();
           // todoListView.getItems().setAll(TodoData.getInstance().getTodoItems());
            if (newItem!=null) {
                todoListView.getSelectionModel().select(newItem);
            }else {
                todoListView.getSelectionModel().selectFirst();
            }
           /* if (newItem.getDeadLine()!=null) {
                todoListView.getSelectionModel().select(newItem);
            }else {
                todoListView.getSelectionModel().selectFirst();
            }*/
            System.out.println("Ok pressed");
        }else {
            System.out.println("Cancel pressed");
        }

    }

    public void handleKeyPressed(KeyEvent keyEvent){
        TodoItem item=todoListView.getSelectionModel().getSelectedItem();
        if (item != null && keyEvent.getCode().equals(KeyCode.DELETE)){
            deleteItem(item);
        }
    }

    public void handleClickListView(){
        TodoItem item=todoListView.getSelectionModel().getSelectedItem();
        textArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadLine().toString());
       /* StringBuilder sb=new StringBuilder(item.getDetails());
        sb.append("\n\n\n");
        sb.append("deadLine : " + item.getDeadLine() + " \n more details...");
        textArea.setText(sb.toString());*/

    }
    }