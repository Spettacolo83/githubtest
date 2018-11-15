package it.stefanorussello.githubtest.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import it.stefanorussello.githubtest.R;
import it.stefanorussello.githubtest.listeners.RepoListener;
import it.stefanorussello.githubtest.models.GithubRepo;

public class RepoAdapter extends ArrayAdapter<GithubRepo> {

    RepoListener repoListener;

    private static class ViewHolder {
        TextView repoTitle;
        TextView repoDesc;
        ImageView repoImage;
    }

    public RepoAdapter(Context context, List<GithubRepo> news, RepoListener listener) {
        super(context, R.layout.repo_list_item, news);
        repoListener = listener;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final GithubRepo repo = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.repo_list_item, parent, false);

            viewHolder.repoTitle = (TextView) convertView.findViewById(R.id.news_title);
            viewHolder.repoDesc = (TextView) convertView.findViewById(R.id.news_description);
            viewHolder.repoImage = (ImageView) convertView.findViewById(R.id.news_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.repoTitle.setText(repo.name);
        viewHolder.repoDesc.setText(repo.description);
        Glide.with(getContext()).load(repo.owner.avatarUrl).into(viewHolder.repoImage);

        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repoListener.itemClicked(position, repo);
            }
        });

        return convertView;
    }
}