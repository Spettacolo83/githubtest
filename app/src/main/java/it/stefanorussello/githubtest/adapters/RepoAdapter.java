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
import it.stefanorussello.githubtest.listeners.RepoClickListener;
import it.stefanorussello.githubtest.models.GithubRepo;

public class RepoAdapter extends ArrayAdapter<GithubRepo> {

    RepoClickListener repoClickListener;

    private static class ViewHolder {
        TextView repoTitle;
        TextView repoAuthor;
        TextView repoDesc;
        ImageView repoImage;
    }

    public RepoAdapter(Context context, List<GithubRepo> repos, RepoClickListener listener) {
        super(context, R.layout.repo_list_item, repos);
        repoClickListener = listener;
    }

    public void refresh(List<GithubRepo> repos) {
        clear();
        int i=0;
        for (GithubRepo repo : repos) {
            insert(repo, i);
            i++;
        }
        this.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final GithubRepo repo = getItem(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.repo_list_item, parent, false);

            viewHolder.repoTitle = convertView.findViewById(R.id.repo_title);
            viewHolder.repoAuthor = convertView.findViewById(R.id.repo_author);
            viewHolder.repoDesc = convertView.findViewById(R.id.repo_description);
            viewHolder.repoImage = convertView.findViewById(R.id.repo_image);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.repoTitle.setText(repo.name);
        viewHolder.repoAuthor.setText(repo.owner.login);
        viewHolder.repoDesc.setText(repo.description);
        Glide.with(getContext()).load(repo.owner.avatarUrl).into(viewHolder.repoImage);

        convertView.setClickable(true);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repoClickListener.itemClicked(position, repo);
            }
        });

        return convertView;
    }
}