package com.example.car_rental_sys.controllers;

import com.example.car_rental_sys.concurrency.AsyncExecutor;
import com.example.car_rental_sys.dao.DAOFactory;
import com.example.car_rental_sys.dao.TodoDAO;
import com.example.car_rental_sys.db.DatabaseConnection;
import com.example.car_rental_sys.db.DatabaseInitializer;
import com.example.car_rental_sys.model.Todo;
import com.example.car_rental_sys.model.TodoPriority;
import com.example.car_rental_sys.model.User;
import com.example.car_rental_sys.service.ServiceFactory;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Admin Sidebar To-Do / Task Management Component.
 * Supports interactive task creation, completion toggling, due date tracking,
 * and maintains continuous dual-slot card presence matching reference design.
 */
public class UIToDoController {

    @FXML
    private Pane pane1;

    @FXML
    private Pane pane2;

    @FXML
    private Label label1;

    @FXML
    private Label label2;

    @FXML
    private Label time1;

    @FXML
    private Label time2;

    @FXML
    private CheckBox checkBox1;

    @FXML
    private CheckBox checkBox2;

    @FXML
    private ImageView addBtn;

    @FXML
    private Pane addTaskPane;

    @FXML
    private TextArea taskContent;

    @FXML
    private DatePicker datePicker;

    private Integer currentTodoId1;
    private Integer currentTodoId2;
    private boolean currentCompleted1 = false;
    private boolean currentCompleted2 = false;

    private final TodoDAO todoDAO = DAOFactory.getInstance().getTodoDAO();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        initEvents();
        hideAddTaskPane();
        loadTodoData();
    }

    private void initEvents() {
        if (pane1 != null) {
            pane1.setOnMouseClicked(event -> toggleToDo1());
        }
        if (pane2 != null) {
            pane2.setOnMouseClicked(event -> toggleToDo2());
        }
        if (checkBox1 != null) {
            checkBox1.setOnMouseClicked(event -> toggleToDo1());
        }
        if (checkBox2 != null) {
            checkBox2.setOnMouseClicked(event -> toggleToDo2());
        }
    }

    private void loadTodoData() {
        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        int userId = user != null ? user.getUserId() : 1;

        AsyncExecutor.getInstance().executeAsync(
                () -> {
                    List<Todo> todos = todoDAO.findByUserId(userId);
                    if (todos == null || todos.size() < 2) {
                        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                            DatabaseInitializer.ensureDefaultTodos(conn);
                            todos = todoDAO.findByUserId(userId);
                        } catch (Exception ignored) {}
                    }
                    return todos;
                },
                todos -> {
                    displayTodos(todos);
                },
                error -> {
                    System.err.println("[UIToDoController] Error loading todos: " + error.getMessage());
                }
        );
    }

    private void displayTodos(List<Todo> todos) {
        if (pane1 == null || pane2 == null) {
            return;
        }

        pane1.setVisible(true);
        pane2.setVisible(true);

        Todo t1 = (todos != null && todos.size() > 0) ? todos.get(0)
                : new Todo(1, "Submit OODJ", "Complete and submit OODJ assignment and audit documentation", TodoPriority.HIGH, LocalDate.of(2026, 12, 16));
        Todo t2 = (todos != null && todos.size() > 1) ? todos.get(1)
                : new Todo(1, "New task", "Quarterly fleet inspection and telemetry audit", TodoPriority.MEDIUM, LocalDate.of(2026, 11, 28));

        bindTodo(t1, label1, time1, checkBox1);
        currentTodoId1 = t1.getTodoId();
        currentCompleted1 = Boolean.TRUE.equals(t1.getIsCompleted());

        bindTodo(t2, label2, time2, checkBox2);
        currentTodoId2 = t2.getTodoId();
        currentCompleted2 = Boolean.TRUE.equals(t2.getIsCompleted());
    }

    private void bindTodo(Todo todo, Label titleLbl, Label timeLbl, CheckBox cb) {
        boolean completed = Boolean.TRUE.equals(todo.getIsCompleted());
        if (titleLbl != null) {
            titleLbl.setText(todo.getTitle() != null ? todo.getTitle() : "Task");
            if (completed) {
                if (!titleLbl.getStyleClass().contains("delete")) {
                    titleLbl.getStyleClass().add("delete");
                }
            } else {
                titleLbl.getStyleClass().remove("delete");
            }
        }
        if (timeLbl != null) {
            String due = todo.getDueDate() != null ? todo.getDueDate().format(DATE_FMT) : "No Due Date";
            timeLbl.setText(due);
        }
        if (cb != null) {
            cb.setSelected(completed);
        }
    }

    private void toggleToDo1() {
        if (currentTodoId1 == null) return;
        boolean newStatus = !currentCompleted1;
        currentCompleted1 = newStatus;
        if (checkBox1 != null) checkBox1.setSelected(newStatus);
        if (label1 != null) {
            if (newStatus) {
                if (!label1.getStyleClass().contains("delete")) label1.getStyleClass().add("delete");
            } else {
                label1.getStyleClass().remove("delete");
            }
        }

        int id = currentTodoId1;
        AsyncExecutor.getInstance().executeAsync(
                () -> todoDAO.markAsCompleted(id, newStatus),
                success -> refreshAfterDelay(),
                error -> System.err.println("[UIToDoController] Failed to toggle todo: " + error.getMessage())
        );
    }

    private void toggleToDo2() {
        if (currentTodoId2 == null) return;
        boolean newStatus = !currentCompleted2;
        currentCompleted2 = newStatus;
        if (checkBox2 != null) checkBox2.setSelected(newStatus);
        if (label2 != null) {
            if (newStatus) {
                if (!label2.getStyleClass().contains("delete")) label2.getStyleClass().add("delete");
            } else {
                label2.getStyleClass().remove("delete");
            }
        }

        int id = currentTodoId2;
        AsyncExecutor.getInstance().executeAsync(
                () -> todoDAO.markAsCompleted(id, newStatus),
                success -> refreshAfterDelay(),
                error -> System.err.println("[UIToDoController] Failed to toggle todo: " + error.getMessage())
        );
    }

    private void refreshAfterDelay() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(800), event -> loadTodoData()));
        timeline.play();
    }

    @FXML
    private void showAddTaskPane() {
        if (addTaskPane == null) {
            return;
        }
        if (addTaskPane.isVisible()) {
            hideAddTaskPane();
            return;
        }
        addTaskPane.setVisible(true);
    }

    @FXML
    private void hideAddTaskPane() {
        if (addTaskPane != null) {
            addTaskPane.setVisible(false);
        }
    }

    @FXML
    private void addTaskBtnClick() {
        if (taskContent == null || taskContent.getText().trim().isEmpty()) {
            return;
        }

        String content = taskContent.getText().trim();
        LocalDate due = datePicker != null && datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now().plusDays(7);

        User user = ServiceFactory.getInstance().getAuthService().getCurrentUser();
        int userId = user != null ? user.getUserId() : 1;

        Todo newTodo = new Todo(userId, content, content, TodoPriority.MEDIUM, due);

        AsyncExecutor.getInstance().executeAsync(
                () -> todoDAO.save(newTodo),
                saved -> {
                    taskContent.clear();
                    if (datePicker != null) {
                        datePicker.setValue(null);
                    }
                    hideAddTaskPane();
                    loadTodoData();
                },
                error -> {
                    System.err.println("[UIToDoController] Error creating todo: " + error.getMessage());
                }
        );
    }
}
