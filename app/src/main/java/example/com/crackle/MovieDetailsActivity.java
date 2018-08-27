package example.com.crackle;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static example.com.crackle.Constants.API_KEY;
import static example.com.crackle.Constants.IMAGE_URL_SIZE;
import static example.com.crackle.Constants.LOG_TAG;

public class MovieDetailsActivity extends AppCompatActivity {

    @BindView(R.id.poster_image)
    ImageView posterImage;
    @BindView(R.id.tmdbRating)
    TextView tmdbRating;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.popularity)
    TextView popularity;
    @BindView(R.id.language)
    TextView language;
    @BindView(R.id.duration)
    TextView duration;
    @BindView(R.id.genre)
    TextView genre;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    private Movie movie;
    private int movieId;

    private MovieApiClient client;
    private Call<DetailResults> call;
    private HashMap<Integer, String> genreMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ButterKnife.bind(this);

        genreMap = Utils.fetchAllGenres(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (getIntent() != null) {
            if (getIntent().hasExtra(Intent.EXTRA_TEXT)) {
                movie = getIntent().getParcelableExtra(Intent.EXTRA_TEXT);
                movieId = movie.getMovieId();
                setTitle(movie.getTitle());

                tmdbRating.setText(DecimalFormat.getNumberInstance().format(movie.getUserRating()).concat("/10"));
                Glide.with(this)
                        .load(IMAGE_URL_SIZE.concat(movie.getImageUrl()))
                        .into(posterImage);
                ratingBar.setRating((float) (movie.getUserRating()/2f));
                popularity.setText(DecimalFormat.getNumberInstance().format(movie.getPopularity()));
                language.setText(movie.getLanguage());

                List<Integer> genreId = new ArrayList<>();
                genreId.addAll(movie.getGenres());
                int count = 0;
                for (int id : genreId) {
                    genre.append(genreMap.get(id));
                    count++;
                    if (count < genreId.size() - 1) {
                        genre.append(", ");
                    }
                }

            }
        }

        if (movieId != 0) {
            client = MovieApiService.getClient().create(MovieApiClient.class);
            call = client.getMovieDetails(movieId, API_KEY);

            call.enqueue(new Callback<DetailResults>() {
                @Override
                public void onResponse(Call<DetailResults> call, Response<DetailResults> response) {
                    int runtime = response.body().getDuration();
                    duration.setText(Utils.formatDuration(runtime));
                }

                @Override
                public void onFailure(Call<DetailResults> call, Throwable t) {
                    Toast.makeText(MovieDetailsActivity.this, "Error getting movie duration", Toast.LENGTH_SHORT).show();
                }
            });
        }

        viewPager.setAdapter(new MovieFragmentPagerAdapter(getSupportFragmentManager(), movie));
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
