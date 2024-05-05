package org.example.todo;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class DialogController {

    @FXML
    private TextField shortDescription;

    @FXML
    private TextArea detailArea;
    @FXML
    private DatePicker deadLinePicker;

    public TodoItem processResult(){
        String sDescription=shortDescription.getText().trim();
        String details=detailArea.getText().trim();
        LocalDate datePicker=deadLinePicker.getValue();

        if (sDescription!=null && details != null && datePicker != null) {
            TodoItem newItem = new TodoItem(sDescription, details, datePicker);
            TodoData.getInstance().addTodoItem(newItem);
            return newItem;
        }else {
            return null;
        }

    }
}
