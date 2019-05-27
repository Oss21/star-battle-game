package controllers;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import model.Player;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

// Clase

/**
 * Entidad que representa el controlador de la ventana del salón de la fama.
 */
public class HallOfFameController implements Initializable {

    // Atrubutos

    /**
     * Es la tabla que muestra los datos del salón.
     */
    @FXML private TableView<Player> tvHallOfFame;

    /**
     * Es la columna que muestra el nombre el nombre de un  jugador.
     */
    @FXML private TableColumn<Player, String> tcName;

    /**
     * Es la columna que muestra el puntaje global de un jugador.
     */
    @FXML private TableColumn<Player, Integer> tcGlobalScore;

    /**
     * Es la columna que muestra las partidas ganadas de un jugador.
     */
    @FXML private TableColumn<Player, Integer> tcWonMatch;

    /**
     * Es la columna que muestra las partidas perdidas de un jugador
     */
    @FXML private TableColumn<Player, Integer> tcLostMatch;

    // Métodos

    /**
     * Carga e inicializa el formato en que muestran los datos en la tabla del salón.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        tcName.setCellValueFactory(new PropertyValueFactory<Player,String>("name"));
        tcGlobalScore.setCellValueFactory(new PropertyValueFactory<Player,Integer>("globalScore"));
        tcWonMatch.setCellValueFactory(new PropertyValueFactory<Player,Integer>("wonMatch"));
        tcLostMatch.setCellValueFactory(new PropertyValueFactory<Player,Integer>("lostMatch"));

    }

    /**
     * Controla la acción de presionar el botón para volver a la ventana de menú principal.
     * @param event - Es el evento producido al presionar el botón.
     */
    @FXML
    void returnClicked(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userInterface/LoginWindowGUI.fxml"));
        Parent root = null;

        try {
            root = loader.load();
        }catch (IOException e){
            e.printStackTrace();
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Iniciar Sesión");
        stage.show();
    }






}