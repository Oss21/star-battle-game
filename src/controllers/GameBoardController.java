package controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import customExceptions.EmptyDataException;
import customExceptions.NotShipsPositionedException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Faction.SpaceShipType;
import model.Game;
import model.Match;
import model.Match.Direction;
import model.Player;
import threads.UpdateThreadMatchTime;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

// Clase

/**
 * Entidad que representa el controlador de la ventana del tablero de juego.
 */
public class GameBoardController implements Initializable {

    // Atrubutos

    /**
     * Relacion con la clase player
     */
    private Player player;

    private Game game;

    /**
     *Se encarga de verificar si el juego ya comenzo, para desabilitar la opcion de agregar naves al tablero de juego del jugador
     */
    private boolean playClicked;

    /**
     * Matriz de botones el cual albergara el tablero del jugador y todos los cambios que se le hagan.
     */
    private JFXButton[][] gameBoardP;

    /**
     * Matriz de botones el cual albergara el tablero de la maquina y todos los cambios que se le hagan
     */
    private JFXButton[][] gameBoardM;

    /**
     *Almacena las naves que, posteriormente, se desplegaran en el tablero de juego del jugador
     */
    @FXML private JFXComboBox<SpaceShipType> spaceShipsBox;

    /**
     *Almacena la orientacion de las naves que se colocaran en el tablero de juego del jugador
     */
    @FXML private JFXComboBox<Direction> horientationBox;

    /**
     *Tablero de juego del jugador donde estaran almacenadas sus naves
     */
    @FXML private GridPane gameBoadPlayer;

    /**
     *Tablero de juego de la maquina donde estaran almacenadas sus naves
     */
    @FXML private GridPane gameBoardMachine;

    /**
     *Muestra en pantalla el tiempo del turno del jugador
     */
    @FXML private Label turnTimePlayer;

    /**
     *Muestra en pantalla el tiempo del turno de la maquina
     */
    @FXML private Label turnTimeMachine;

    /**
     *Muestra en pantalla el tiempo de la partida en curso
     */
    @FXML private Label gameTime;


    /**
     * Permite cambiar la acción del boton de posicionar las naves, cuando inicie el juego.
     */
    private boolean paint;

    /**
     *Carga e inicializa los contenedores donde estaran las naves que seran agregadas y la orientacion de estas, ademas,
     * inicializa las matrices para su debida manipulacion.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playClicked = false;
        paint = true;
        spaceShipsBox.getItems().addAll(SpaceShipType.SHUTTLE, SpaceShipType.BOMBER, SpaceShipType.INTERCEPTOR, SpaceShipType.GUNSHIP, SpaceShipType.STARFIGHTER, SpaceShipType.DESTROYER, SpaceShipType.BATTLECRUISER, SpaceShipType.DREADNOUGHT);
        horientationBox.getItems().addAll(Direction.HORIZONTAL, Direction.VERTICAL);

        gameBoardP = new JFXButton[Match.GAME_BOARD_SIZE][Match.GAME_BOARD_SIZE];
        gameBoardM = new JFXButton[Match.GAME_BOARD_SIZE][Match.GAME_BOARD_SIZE];

        gameBoardMachine.setDisable(true);
        fillGrid();
    }



    /**
     * Controla el evento de comenzar a jugar la partida en curso
     * @param event Es el evento producido al presionar el boton
     */
    @FXML
    void playClicked(ActionEvent event) {
        if (!spaceShipsBox.getItems().isEmpty()){
            NotShipsPositionedException np = new NotShipsPositionedException();
            np.message();

        }else {
            playClicked = true;

            UpdateThreadMatchTime umt = new UpdateThreadMatchTime(this);
            umt.setDaemon(true);
            umt.start();

            gameBoadPlayer.setDisable(true);
            gameBoardMachine.setDisable(false);
            fillGridMachine();
            paint = false;
        }
    }

    /**
     * Se encarga de guardar la partida cuando el jugador lo desee.
     * @param event Es el evento que se produce al hacer click en el boton
     */
    @FXML
    void saveMatch(ActionEvent event) {

    }

    /**
     * Actualiza en la interfaz el tiempo de la partida en curso.
     */
    public void updateMatchTime(){
        String time = player.getMatch().manageMatchTime();
        gameTime.setText(time);

        if (player.getMatch().getTime() <= 0){
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "", ButtonType.FINISH);
            alert.setHeaderText("Se te ha acabado el tiempo, has perdido la partida");

            Optional<ButtonType> option = alert.showAndWait();
            if (option.get() == ButtonType.FINISH){
                player.createRecord();
            }
        }
    }

    /**
     * Se encarga de agregar los botones al tablero de juego del jugador, para poder posicionar las naves propias.
     * <pre> la matriz gameBoardP es != null</>
     * <pre> El gridPane, el cual contendra y mostrara en pantalla los botones del tablero de juego del jugador, es != null</>
     */
    private void fillGrid(){
        for (int i = 0; i < Match.GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < Match.GAME_BOARD_SIZE; j++) {

                JFXButton b = new JFXButton("*");
                b.setId(i + "," + j);
                //Accion del tablero del jugador.
                b.setOnAction(event -> action(b));
                b.setPrefHeight(50);
                b.setPrefWidth(50);

                gameBoardP[i][j] = b;
                gameBoadPlayer.add(b, j, i);
            }
        }
    }

    /**
     * Se encarga de realizar la accion de cuando se presiona uno de los botones del tablero de juego del jugador, en la etapa de cuando esta posicionando las naves.
     * @param b Es el boton seleccionado
     */
    private void action(JFXButton b){
        //Permite desativar la acción de posicionar las naves en el tablero, cuando empieze el juego.
        if(paint) {
            if (!playClicked) {
                SpaceShipType s = spaceShipsBox.getValue();

                try {
                    player.getMatch().createSpaceShips(s, horientationBox.getValue(), b.getId());
                    spaceShipsBox.getItems().remove(s);

                    updateGameBoard(gameBoardP);
                } catch (EmptyDataException e) {
                    e.message();
                }
            }
        }else{
            gameBoadPlayer.setDisable(true);
            gameBoardMachine.setDisable(false);
        }
    }

    /**
     * Se encarga de actualizar lo(s) tablero(s) de juego al ejecutarse alguna accion sobre ello(s)
     * @param gameBoard Es el tablero de juego ha actualizar
     */
    private void updateGameBoard(JFXButton[][] gameBoard){
        for (int i = 0; i < Match.GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < Match.GAME_BOARD_SIZE; j++) {
                //Permite cambiar el color de las casillas que contienen partes de la nave.
                String value = player.getMatch().getGameBoardPlayer()[i][j];
                JFXButton button = gameBoard[i][j];
                if(paint){
                    //Solo funciona cuando el jugador posiciona las naves.
                    // Se convierte en falso cuando comienza el juego.
                    if (value.equals("X")) {
                        button.setText("X");
                        button.setStyle("-fx-background-color:blue");
                        button.setTextFill(Color.WHITE);
                    }
                }else{



                    if(value.equals("#")){
                        button.setText("#");
                        button.setDisable(true);
                        button.setStyle("-fx-background-color:yellow");
                    }
                }




            }
        }
    }

    /**
     * Permite actualizar el tablero cuando encuentre un # el cual represente una casilla sin partes de la nave.
     * @param gameBoard Matriz de botones referente a la de la maquina.
     */
    private void updateBoardMachine(JFXButton[][] gameBoard){
        for (int i = 0; i < Match.GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < Match.GAME_BOARD_SIZE; j++) {
                String value = player.getMatch().getGameBoardMachine()[i][j];
                JFXButton button = gameBoard[i][j];
                if(value.equals("#")){
                    button.setText("#");
                    button.setStyle("-fx-background-color:yellow");
                    button.setDisable(true);

                }
            }
        }
    }
    /**
     * Se encarga de crear los botones que tendra el tablero de juego de la maquina.
     */
    private void fillGridMachine(){
        for (int i = 0; i < Match.GAME_BOARD_SIZE; i++) {
            for (int j = 0; j < Match.GAME_BOARD_SIZE; j++) {

                JFXButton b = new JFXButton("*");
                b.setId(i + "," + j);
                b.setOnAction(event -> {
                    actionPlayer(b.getId());
                    //permite que la maquina reliace su respectiva jugada.
                    turnMachine();
                    //permite actualizar el tablero del jugador.
                    updateGameBoard(gameBoardP);
                    //permite actualizar el tablero del jugador.
                    updateBoardMachine(gameBoardM);
                });
                b.setPrefHeight(50);
                b.setPrefWidth(50);

                gameBoardM[i][j] = b;
                gameBoardMachine.add(b, j, i);
            }
        }
    }

    /**
     * Se encarga de verificar si en la casilla seleccionada por el jugador se encuentra una nave.
     * @param position Es la posicion de la casilla seleccionada
     */
    private void actionPlayer(String position) {
        //La posiciones i y j del tablero
        String[] pos = position.split(",");
        int i = Integer.parseInt(pos[0]);
        int j = Integer.parseInt(pos[1]);
        JFXButton button = gameBoardM[i][j];
        //Retorna true si encuentra en la posición una parte de la nave y false si no la hay nada.
        boolean isShip = player.getMatch().uncoverMachineBox(position);
        if (isShip){
            button.setText("X");
            button.setTextFill(Color.WHITE);
            button.setStyle("-fx-background-color:red");
            button.setDisable(true);

        }else{
            button.setText("#");
            button.setStyle("-fx-background-color:yellow");
            button.setDisable(true);
            //Permite desativar el tablero de la maquina para que ella realice su jugada.
            gameBoardMachine.setDisable(true);
            gameBoadPlayer.setDisable(false);

        }
    }

    /**
     * Se encarga de destapar la casilla al seleccionarse y, al verificarse, que haya una nave en ella(la casilla).
     * @param position Posicion de la casilla seleccionada
     * @param gameBoard Tablero de juego ha acualizar
     */
    private void discoverShip(String position, Button[][] gameBoard){

        boolean founded = false;

        for (int i = 0; i < Match.GAME_BOARD_SIZE && !founded; i++) {
            for (int j = 0; j < Match.GAME_BOARD_SIZE && !founded; j++) {
                if (position.compareTo(gameBoard[i][j].getId()) == 0){
                    founded = true;

                    gameBoard[i][j].setText("X");
                }
            }
        }
    }

    /**
     * Se encarga de escoger aleatoriamente un boton en el tablero de juego del jugador y destaparla para, posteriormente,
     * verificar si hay o no una nave.
     */
    private void turnMachine(){
        String position = player.getMatch().generatePositionMachine();


        String[] p = position.split(",");
        int i = Integer.parseInt(p[0]);
        int j = Integer.parseInt(p[1]);
        JFXButton b = gameBoardP[i][j];

        boolean found = player.getMatch().uncoverPlayersBox(position);
        //Si encuentra una parte de la nave entonces desativa la casilla
        // de lo contrario desativa la casilla,le cambia el texto de valor y cambia de turno
        if (found){
            b.setStyle("-fx-background-color:red");
            b.setTextFill(Color.WHITE);
            b.setDisable(true);
            turnMachine();
        }else{
            b.setText("#");
            b.setDisable(true);
            b.setStyle("-fx-background-color:yellow");
            gameBoadPlayer.setDisable(true);
            gameBoardMachine.setDisable(false);

        }
    }
    /**
     * Cambia al jugador que posea la relacion player
     * <pre> el parametro player es != null</>
     * @param player Es el jugador al cual se cambiara
     */
    public void setPlayer(Player player){
        this.player = player;
    }

    /**
     * Devuelve el jugador que posea la relacion player
     * @return El jugador que posea la relacion player
     */

    public Player getPlayer(){
        return player;
    }


    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }





    @FXML
    void backClicked(ActionEvent event) {

        try {
            game.saveStateGame();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/userInterface/MainMenuGUI.fxml"));
        Parent root = null;

        try{
            root = loader.load();
        }catch (IOException e){
            e.printStackTrace();
        }

        MainMenuController ven = loader.getController();
        ven.setGame(this.game);
        ven.setPlayer(this.player);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("Menu principal");
        stage.show();
    }


}
