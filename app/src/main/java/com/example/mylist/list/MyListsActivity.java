package com.example.mylist.list;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylist.R;
import com.example.mylist.database.AppDatabase;
import com.example.mylist.model.Product;
import com.example.mylist.model.Shoppinglist;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Permet d'afficher la liste des listes de courses de l'utilisateur
 */
public class MyListsActivity extends AppCompatActivity {

    private AppDatabase db;
    private SearchView searchView;

    TextInputEditText shoplistName;
    ArrayList<Shoppinglist> shoppinglists;
    Button deleteShoppinglists;
    ListView myListsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_lists);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().setTitle("Mes Listes");
        initSearchWidgets();

        shoplistName = findViewById(R.id.eShoppinglistName);
        myListsView = findViewById(R.id.myListsView);
        deleteShoppinglists = findViewById(R.id.delAllLists);

        db = AppDatabase.getInstance(this);
        shoppinglists = (ArrayList<Shoppinglist>) db.shoppinglistItemDao().getAll();

        MyListsActivity.AndroidAdapter adapter = new MyListsActivity.AndroidAdapter(this, R.layout.row_my_lists, shoppinglists);
        myListsView.setAdapter((ListAdapter) adapter);
    }

    /**
     * Ajoute un enregistrement dans la table "shoppinglist" en BD
     * @param view
     */
    public void shoppinglistRegistration(View view) {
        shoppinglists = (ArrayList<Shoppinglist>) db.shoppinglistItemDao().getAll();
        String name = shoplistName.getText().toString();
        if (name.length() > 2) {

            if (shoppinglists.isEmpty()) {
                addShoppinglist(name);
            } else {
                for (int i = 0; i < shoppinglists.size(); i++) {
                    if (shoppinglists.get(i).getName().toLowerCase().equals(name.toLowerCase())) {
                        Toast.makeText(this, "Nom déjà utilisé, veuillez réessayer", Toast.LENGTH_LONG).show();
                        break;
                    } else {
                        addShoppinglist(name);
                    }
                }
            }
        } else {
            Toast.makeText(this,"Nom invalide, 3 caractères minimum", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Ajoute la liste de courses dont le nom est passé en paramètre
     * @param name
     */
    public void addShoppinglist(String name) {
        Shoppinglist shoppinglist = new Shoppinglist(name);
        db.shoppinglistItemDao().insert(shoppinglist);
        Intent intent = new Intent(this, MyListActivityWs.class);
        intent.putExtra("listname", name);
        startActivity(intent);
    }

    /**
     * Permet de filtrer dynamiquement en fonction de ce qui est entré dans le champs de recherche
     */
    private void initSearchWidgets() {
        searchView = (SearchView) findViewById(R.id.shapeListSearchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) // s corresponds au texte saisie dans le champs de recherche
            {
                // products corresponds à la liste affichée au début
                ArrayList<Shoppinglist> shoppinglists = (ArrayList<Shoppinglist>) db.shoppinglistItemDao().getAll();

                // filteredShapes corresponds à la liste des objets filtrés
                ArrayList<Shoppinglist> filteredShapes = new ArrayList<Shoppinglist>();

                for(Shoppinglist shoppinglist : shoppinglists)
                {
                    // Comparaison du nom, de la catégorie, du magasin, à la chaine tapée dans le searchView
                    if (shoppinglist.getName().toLowerCase().contains(s.toLowerCase())) {
                        filteredShapes.add(shoppinglist);
                    }
                }

                MyListsActivity.AndroidAdapter adapter = new MyListsActivity.AndroidAdapter(getApplicationContext(), R.layout.row_my_lists, filteredShapes);
                myListsView.setAdapter(adapter);

                return false;
            }
        });
    }

    /**
     * Adapter personnalisé pour l'affichage de la liste des "shoppinglist" (stockés en BD locale)
     */
    public class AndroidAdapter extends ArrayAdapter<Shoppinglist> {

        private Context context;
        private int resource;
        private int textViewResourceId;
        private ArrayList<Shoppinglist> shoppinglists;

        /**
         * Constructeur
         * @param context
         * @param resource
         * @param shoppinglists
         */
        public AndroidAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Shoppinglist> shoppinglists) {
            super(context, resource, shoppinglists);
            this.context = context;
            this.resource = resource;
            this.shoppinglists = shoppinglists;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if(convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_my_lists, parent, false);
            }

            LinearLayout lineList = convertView.findViewById(R.id.lineListsView);

            lineList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String nomList = shoppinglists.get(position).getName();
                    Intent i = new Intent(getApplicationContext(), MyListActivity.class);
                    i.putExtra("listname", nomList);
                    startActivity(i);
                }
            });

            TextView textnom = convertView.findViewById(R.id.lList);
            TextView textshared = convertView.findViewById(R.id.lShared);

            String nomList = shoppinglists.get(position).getName();
            boolean sharedList = shoppinglists.get(position).getShared();
            textnom.setText(nomList);
            if (sharedList) {
                textshared.setText("Partagé");
            } else {
                textshared.setText("Non partagé");
            }

            ImageView imageDelete = convertView.findViewById(R.id.deleteList);

            /**
             * Supprime la "shoppinglist" cliquée
             */
            imageDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int idList = shoppinglists.get(position).getId();
                    List<Product> products = db.productItemDao().getProductByShoppingListId(idList);
                    // Suppression de la liste
                    db.shoppinglistItemDao().delete(db.shoppinglistItemDao().findById(idList));
                    // Suppression des produits de la liste supprimée ci-dessus
                    db.productItemDao().deleteAll(products);
                    // Rafraîchissement de la vue
                    shoppinglists.remove(position);
                    myListsView.invalidateViews();
                }
            });

            /**
             * Supprime toutes les "shoppinglist" lors du clic
             */
            deleteShoppinglists.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle("Supprimer les listes");
                    builder.setMessage("Etes vous certains de vouloir supprimer vos listes ?");
                    builder.setPositiveButton("Confirmer",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    List<Product> products = db.productItemDao().getAll();
                                    db.productItemDao().deleteAll(products);
                                    List<Shoppinglist> shoppinglists = db.shoppinglistItemDao().getAll();
                                    db.shoppinglistItemDao().deleteAll(shoppinglists);
                                    Intent i = new Intent(getContext(), MyListsActivity.class);
                                    startActivity(i);
                                    Toast.makeText(getContext(),"Vos listes ont bien été supprimées", Toast.LENGTH_SHORT).show();
                                }
                            });
                    builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            return convertView;
        }
    }

}