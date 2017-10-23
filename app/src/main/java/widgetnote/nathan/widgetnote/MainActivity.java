package widgetnote.nathan.widgetnote;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public final class MainActivity extends AppCompatActivity {
    private ArrayList<String> listTemp = new ArrayList<>();
    private ArrayAdapter<String> listaAdapter;
    private ListView lista;
    private PopupWindow textInput;
    private Button addButton;
    private EditText input;
    private int id = 0;
    private View.OnClickListener add_button_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            if (input.getText().toString().isEmpty()) {
                textInput.dismiss();
                Toast.makeText(getApplicationContext(), "Text field empty.Try again.", Toast.LENGTH_SHORT).show();
            } else {
                listTemp.add(input.getText().toString());
                id++;
                textInput.dismiss();
                listaAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), listTemp.get(id - 1), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lista = (ListView) findViewById(R.id.lista);
        listaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listTemp);
        lista.setAdapter(listaAdapter);
        lista.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int itemPosition = position;
                PopupMenu itemMenu = new PopupMenu(getApplicationContext(), view);
                itemMenu.inflate(R.menu.item_menu);
                itemMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if (id == R.id.delete_option) {
                            listaAdapter.remove(listaAdapter.getItem(itemPosition));
                            listaAdapter.notifyDataSetChanged();
                        }
                        if (id == R.id.edit_option) {
                            //in development
                        }
                        return true;
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listTemp.addAll(restoreListState());
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveListState(listTemp);
    }

    //Função que recebe o array de strings da função EditTextExtractor() e o salva em um arquivo
    // privado na memória interna do dispositivo para restaurar a lista em usos posteriores do app.
    public void saveListState(ArrayList<String> data) {
        FileOutputStream listState;
        ObjectOutputStream outputStreamWriter = null;
        try {
            listState = openFileOutput("state.txt", MODE_PRIVATE);
            outputStreamWriter = new ObjectOutputStream(listState);
            outputStreamWriter.writeObject(data);
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        } finally {
            if (outputStreamWriter != null)
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    //Cria popup de criação de anotação
    private void initiatePopupWindow() {
        try {

            //We need to get the instance of the LayoutInflater, use the context of this activity
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //Inflate the view from a predefined XML layout
            View layout = inflater.inflate(R.layout.text_input,
                    (ViewGroup) findViewById(R.id.textinput_container_layout));
            // create a 450px width and dynamic height PopupWindow
            textInput = new PopupWindow(layout, 450, WRAP_CONTENT, true);
            // display the popup in the center
            textInput.showAtLocation(layout, Gravity.CENTER, 0, 0);

            input = (EditText) layout.findViewById(R.id.text_input);
            input.setRawInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            input.setImeOptions(EditorInfo.IME_ACTION_DONE);

            addButton = (Button) layout.findViewById(R.id.add_text_button);
            addButton.setOnClickListener(add_button_click_listener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Função auxiliar para a restauração do estado do ArrayList com os itens da lista criada pelo
    // usuário
    public List<String> restoreListState() {

        ArrayList<String> recovery = new ArrayList<>();
        FileInputStream fis;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput("state.txt");
            ois = new ObjectInputStream(fis);
            recovery = (ArrayList<String>) ois.readObject();

        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        } catch (ClassNotFoundException e) {
            Log.e("List recovery", "Can not restore the list: " + e.toString());
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return recovery;
    }

    public void clearList() {
        id = 0;
        listTemp.clear();
        saveListState(listTemp);
        listaAdapter.notifyDataSetChanged();
    }

    //Função que define a ação do botão de adição de item na lista (ImageButton)
    public void onClick(View view) {
        this.initiatePopupWindow();
    }

    //Cria Menu a partir do layout XML
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    //Define o comportamento do menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        //Limpa a lista de itens
        if (id == R.id.clean_list) {
            this.clearList();
        }
        return super.onOptionsItemSelected(item);
    }
}