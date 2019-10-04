package codes.umair.wallbox.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.List;

import codes.umair.wallbox.R;
import codes.umair.wallbox.adapters.ImageListAdapter;
import codes.umair.wallbox.api.APIClient;
import codes.umair.wallbox.api.APIInterface;
import codes.umair.wallbox.models.Post;
import codes.umair.wallbox.models.PostList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ImageListAdapter.OnItemClickListener {

    public static final String EXTRA_URL = "imageUrl";
    public static final String EXTRA_CREATOR = "creatorName";
    public static final String EXTRA_SIZE = "imgSize";
    public static final String EXTRA_LIKES = "likeCount";
    public static final String EXTRA_VIEWS = "viewsCount";
    APIInterface apiInterface;
    RecyclerView rv;
    SwipeRefreshLayout mSwipeRefresher;
    ConstraintLayout root;
    List<Post> hits;
    /*
    Created by Umair Ayub on 17 Sept 2019.
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = findViewById(R.id.rv);
        mSwipeRefresher = findViewById(R.id.mSwipeRefresh);
        root = findViewById(R.id.root);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());


        if (isNetworkAvailable()) {
            LoadImages("");
        } else {
            mSwipeRefresher.setRefreshing(false);
            rv.setVisibility(View.INVISIBLE);
            Snackbar snackbar = Snackbar
                    .make(rv, "No connection, Try Again", Snackbar.LENGTH_LONG);
            snackbar.show();
        }

        mSwipeRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    LoadImages("");
                } else {
                    mSwipeRefresher.setRefreshing(false);
                    rv.setVisibility(View.INVISIBLE);
                    Snackbar snackbar = Snackbar
                            .make(rv, "No connection, Try Again", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

    }

    public void LoadImages(String query) {

        HashMap<String, String> map = new HashMap<>();

        map.put("key", "13799911-62a795ec2e29137d307467722");
        map.put("orientation", "vertical");
        map.put("per_page", "200");
        if (!query.equals("")){
            map.put("q", query);
        }
        mSwipeRefresher.setRefreshing(true);
        apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<PostList> call = apiInterface.getImageResults(map);
        call.enqueue(new Callback<PostList>() {
            @Override
            public void onResponse(Call<PostList> call, Response<PostList> response) {
                PostList postList = response.body();
                hits = postList.getPosts();
                ImageListAdapter adapter = new ImageListAdapter(getApplicationContext(), hits);
                rv.setAdapter(adapter);
                rv.setVisibility(View.VISIBLE);
                adapter.setOnItemClickListener(MainActivity.this);
                mSwipeRefresher.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<PostList> call, Throwable t) {
                Snackbar snackbar = Snackbar
                        .make(root, "Error requesting server, Please try again", Snackbar.LENGTH_LONG);
                snackbar.show();
                mSwipeRefresher.setRefreshing(false);
            }
        });

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LoadImages(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newQuery) {
                return true;
            }
        });
        return true;
    }

    @Override
    public void onItemClick(int position) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        Post clickedItem = hits.get(position);

        detailIntent.putExtra(EXTRA_URL, clickedItem.getFullHDURL());
        detailIntent.putExtra(EXTRA_CREATOR, clickedItem.getUser());
        detailIntent.putExtra(EXTRA_SIZE, "W " + clickedItem.getImageWidth() + " x H " + clickedItem.getImageHeight());
        detailIntent.putExtra(EXTRA_LIKES, clickedItem.getLikes());
        detailIntent.putExtra(EXTRA_VIEWS, clickedItem.getViews());

        startActivity(detailIntent);
    }
}