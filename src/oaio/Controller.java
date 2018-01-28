package oaio;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

public class Controller implements Initializable {

    public ComboBox npc_move;
    public TextField charname_move;
    @FXML
    private Button go1 = new Button();
    @FXML
    private Button del = new Button();
    @FXML
    private TextField char1 = new TextField();
    @FXML
    private TextField plus = new TextField();
    @FXML
    private TextField char2 = new TextField();
    @FXML
    private ComboBox from;
    @FXML
    private ComboBox to;
    @FXML
    private TextField fee = new TextField();
    @FXML
    private TableView rateView = new TableView();
    @FXML
    private TextField chartele = new TextField();
    @FXML
    private Label stucklabel = new Label();
    @FXML
    private TextField skchar = new TextField();
    @FXML
    private TextField silkacc = new TextField();
    @FXML
    private TextField silkamount = new TextField();
    @FXML
    private TextField charinv = new TextField();
    @FXML
    private TextField charlvl = new TextField();
    @FXML
    private TextField charpk = new TextField();
    @FXML
    private TextField dmob = new TextField();
    @FXML
    private TextField ditem = new TextField();
    @FXML
    private TextField dratio = new TextField();
    @FXML
    private TextField dmin = new TextField();
    @FXML
    private TextField dmax = new TextField();
    
    
    PreparedStatement pst;
    ResultSet rs = null;
    Connection conn = null;
    Statement stmt = null;
    private ObservableList<ObservableList> rates;

    @FXML
    private void handleFB(ActionEvent event) throws SQLException, IOException {

        try (Connection conn = Sql.DbConnector();) {
            String FBQuery = Files.lines(Paths.get("sql/FB.txt")).collect(Collectors.joining("\n"));

            pst = conn.prepareStatement(FBQuery);
            pst.setString(1, char1.getText());
            pst.setString(2, plus.getText());
            pst.execute();
        }
    }

    @FXML
  private void handleTELE(ActionEvent event) throws SQLException, IOException {
        try (Connection conn = Sql.DbConnector ( )) {
            String TELEQuery = Files.lines (Paths.get ("sql/TELE.txt")).collect (Collectors.joining ("\n"));
            pst = conn.prepareStatement (TELEQuery);
            pst.setString (1, (String) from.getSelectionModel ( ).getSelectedItem ( ));
            pst.setString (2, (String) to.getSelectionModel ( ).getSelectedItem ( ));
            pst.setString (3, fee.getText ( ));

            String MakeTXT = "USE SRO_R_SHARD Select * from _RefTeleLink";
            
            Statement stm = conn.createStatement ( );
            ResultSet rs = stm.executeQuery (MakeTXT);
            List<String> rows = new ArrayList<> ( );
            StringBuilder row = new StringBuilder ( );
            ResultSetMetaData meta = rs.getMetaData ( );
            final int colCount = meta.getColumnCount ( );
            while (rs.next ( )) {
                row.setLength (0);
                for (int c = 1; c <= colCount; c++) {
                    row.append (rs.getString (c)).append ("\t");
                    if (c == colCount) {
                        row.append (rs.getString (c)).append ("");
                    }
                }
                rows.add (row.toString ( ));
            }
            rs.close ( );
            stm.close ( );
            PrintWriter pw = new PrintWriter (new FileOutputStream ("teleportlink.txt"));
            StringBuilder all = new StringBuilder ( );
            for (String str : rows) {
                all.append (str + "\n");
            }
            pw.print (all);
            pw.close ( );
        }
    }

    @FXML
    private void handleDEL(ActionEvent event) throws SQLException, IOException {
        try (Connection conn = Sql.DbConnector()) {
            String DELQuery = Files.lines(Paths.get("sql/DEL.txt")).collect(Collectors.joining("\n"));

            pst = conn.prepareStatement(DELQuery);
            pst.setString(1, char2.getText());
            pst.execute();
        }
    }

    @FXML
    private void handleNPC_MOVE(ActionEvent event) throws SQLException, IOException {
        System.out.println (npc_move.getSelectionModel ().getSelectedItem ().toString () );
        try (Connection conn = Sql.DbConnector()) {

            String NPCQuery = Files.lines(Paths.get("sql/NPCMOVE.txt")).collect(Collectors.joining("\n"));
            pst = conn.prepareStatement(NPCQuery);
            pst.setString(1, npc_move.getSelectionModel().getSelectedItem().toString ());
            pst.setString(2, charname_move.getText ());
            pst.execute();
        }
    }

    @FXML
    private void handleCHECK(ActionEvent event) throws SQLException, IOException {
        Connection c;
        rates = FXCollections.observableArrayList();
        try (Connection conn = Sql.DbConnector();) {

            String SQL = Files.lines(Paths.get("sql/CHECKRATE.txt")).collect(Collectors.joining("\n"));
            ResultSet rs = conn.createStatement().executeQuery(SQL);

            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                final int j = i;
                TableColumn col = new TableColumn(rs.getMetaData().getColumnName(i + 1));
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
                    public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) {
                        return new SimpleStringProperty(param.getValue().get(j).toString());
                    }
                });
                rateView.getColumns().addAll(col);
            }
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    row.add(rs.getString(i));
                }

                rates.add(row);
            }
            rateView.setItems(rates);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error on Building Data");
        }
    }

    @FXML
    private void TELERefresh(ActionEvent event) throws SQLException, IOException {
        try (Connection conn = Sql.DbConnector();) {

            String query = "USE SRO_R_SHARD Select CodeName128 from _RefTeleport";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            ArrayList<String> teleporters = new ArrayList<>();

            while (rs.next()) {
                teleporters.add(rs.getString(1));
            }
            from.setItems(FXCollections.observableArrayList(teleporters));
            to.setItems(FXCollections.observableArrayList(teleporters));
            rs.close();
        }
    }

    @FXML
    private void NPCrefresh(ActionEvent event) throws SQLException, IOException {
        try (Connection conn = Sql.DbConnector();) {

            String query = "USE SRO_R_SHARD select CodeName128 from _RefObjCommon where CodeName128 like 'NPC%'";
            pst = conn.prepareStatement(query);
            rs = pst.executeQuery();

            ArrayList<String> npcs = new ArrayList<>();

            while (rs.next()) {
                npcs.add(rs.getString(1));
            }
            npc_move.setItems(FXCollections.observableArrayList(npcs));
            rs.close();
        }
    }

    @FXML
    private void handleSTUCK(ActionEvent event) throws IOException {
        try (Connection conn = Sql.DbConnector();) {

            String query = "USE SRO_R_SHARD UPDATE _Char SET LatestRegion=25000,PosX=992,PosY=-32.60888,PosZ=1317 WHERE CharName16=?";
            pst = conn.prepareStatement(query);
            pst.setString(1, chartele.getText());
            pst.execute();
            stucklabel.setText("Success");

        } catch (SQLException e) {
            stucklabel.setText("Failed");
            System.err.println(e);
        }
    }

    @FXML
    private void handleSkillsUPDATE() {
        
            try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/SKILLUPDATE.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                pst.setString(1, skchar.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }
    
    @FXML
    private void handleSkillsGM() {
            
            try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/GMSKILL.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                pst.setString(1, skchar.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }
    
    @FXML
    private void handleSILKS() {
        try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/SILKADD.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                pst.setString(1, silkacc.getText());
                pst.setString(2, silkamount.getText());
                pst.setString(3, silkamount.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }
    
    @FXML
    private void handleINV() {
        try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/INVCLEAN.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                pst.setString(1, charinv.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }
    
    @FXML
    private void handleMaxLVL() {
        try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/MAXLVL.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                pst.setString(1, charlvl.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }
        
    @FXML
    private void handlePK() {
        try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/CHARPK.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                pst.setString(1, charpk.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }
    
    @FXML
    private void handleDROPS() {
        try (Connection conn = Sql.DbConnector();) {
                String query = Files.lines(Paths.get("sql/DROPADD.txt")).collect(Collectors.joining("\n"));
                pst = conn.prepareStatement(query);
                
                pst.setString(1, dmob.getText());
                pst.setString(2, ditem.getText());
                pst.setString(3, dratio.getText());
                pst.setString(4, dmin.getText());
                pst.setString(5, dmax.getText());
                pst.execute();
            } catch (Exception e) {
                System.out.println("Failed");
                System.err.println(e);
            }
    }

    
    
    public void initialize(URL location, ResourceBundle resources) {
        
    }


}
