package FragmentControllers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;
import com.parse.starter.R;
import com.parse.starter.ViewControllers.TrainerViewController;

import java.util.List;

import ConfigClasses.MyProfilePictureView;
import ConfigClasses.ParseAdapterCustomList;
import Models.User;

/**
 * Created by Dylan Castanhinha on 3/31/2017.
 */

public class AddNewClientsOrTrainerFragment extends Fragment {

    TextView labelTV;
    ListView listview;
    NewClientSearch adapter;
    User currentUser;
    SwipeRefreshLayout swipeContainer;
    OnUserSelected activityCallBack;

    public interface OnUserSelected {
        public void onUserSelected(String userId);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            activityCallBack = (OnUserSelected) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_new_clients, container, false);

        //Toolbar top
        final Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.tool_bar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
                toolbar.setNavigationIcon(null);
            }
        });

        //View items
        swipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container_for_new_client);
        currentUser = (User) ParseUser.getCurrentUser();
        listview = (ListView) rootView.findViewById(R.id.add_new_client_list_view);
        adapter = new NewClientSearch(getActivity());
        labelTV = new TextView(getActivity());
        labelTV.setText("Local Clients");
        listview.addHeaderView(labelTV);
        listview.setAdapter(adapter);

        // Configure Swipe to Refresh
        swipeContainer.setOnRefreshListener(new SwipeToRefresh());
        swipeContainer.setColorSchemeResources(R.color.palette_lightprimarycolor);
        return rootView;

    }

    class NewClientSearch extends ParseAdapterCustomList implements ParseQueryAdapter.OnQueryLoadListener {
        Context context;

        public NewClientSearch(Context context){
            super(context, new ParseQueryAdapter.QueryFactory<User>(){
                public ParseQuery<User> create() {
                    ParseQuery<User> query = ParseQuery.getQuery(User.class);
                    query.whereEqualTo("trainerstatus", false);
                    query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
                    return query;
                }
            });
            this.context = context;
            addOnQueryLoadListener(this);
        }
        @Override
        public void onLoading() {
            swipeContainer.setRefreshing(true);
        }
        @Override
        public void onLoaded(List objects, Exception e) {
            swipeContainer.setRefreshing(false);
        }
        @Override
        public View getItemView(final User user, View v, ViewGroup parent) {
            if (v == null){
                v = View.inflate(getContext(), R.layout.list_layout_current_clients, null);
            }
            super.getItemView(user, v, parent);

            //Add the title view
            TextView nameTextView = (TextView) v.findViewById(R.id.current_client_text_view_name);
            nameTextView.setText(user.getFullName());

            //Add the objectid
            TextView objectId = (TextView) v.findViewById(R.id.current_client_object_id);
            objectId.setText(user.getLocation());

            //Add the image
            MyProfilePictureView imageView = (MyProfilePictureView) v.findViewById(R.id.imageView3);
            imageView.setImageBitmap(imageView.getRoundedBitmap(user.getProfilePicture()));

            //On click listener for selection
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activityCallBack.onUserSelected(user.getObjectId());
                }
            });
            return v;
        }
    }
    private class SwipeToRefresh implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh() {
            adapter.loadObjects();
        }
    }

}
