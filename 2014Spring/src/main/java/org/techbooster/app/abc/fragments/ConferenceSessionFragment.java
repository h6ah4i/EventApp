package org.techbooster.app.abc.fragments;

import android.os.Bundle;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.devspark.progressfragment.ProgressFragment;
import com.google.gson.reflect.TypeToken;
import com.sys1yagi.indirectinjector.IndirectInjector;

import org.techbooster.app.abc.R;
import org.techbooster.app.abc.loaders.ConferenceSessionLoader;
import org.techbooster.app.abc.models.ConferenceSession;
import org.techbooster.app.abc.tools.GsonParcer;
import org.techbooster.app.abc.views.ConferenceSessionAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class ConferenceSessionFragment extends ProgressFragment {

    private final static String STATE_KEY_TRACK_SESSION = "track_session";

    private final static String ARGS_URL = "url";

    public static ConferenceSessionFragment newInstance(String url) {
        ConferenceSessionFragment fragment = new ConferenceSessionFragment();

        Bundle args = new Bundle();
        args.putString(ARGS_URL, url);
        fragment.setArguments(args);

        return fragment;
    }

    private String mUrl;

    @InjectView(R.id.list_view)
    ListView mListView;

    private List<ConferenceSession> mConferenceSessions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrl = getArguments().getString(ARGS_URL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IndirectInjector.inject(getActivity(), this);
        setContentView(R.layout.fragment_conference_session);
        ButterKnife.inject(this, getView());

        setContentShown(false);
        if (savedInstanceState != null) {
            mConferenceSessions =
                    GsonParcer.unwrap(savedInstanceState.getParcelable(STATE_KEY_TRACK_SESSION),
                            new TypeToken<Collection<ConferenceSession>>() {
                            }
                    );
            setupSessionList(mConferenceSessions);
        } else {
            new ConferenceSessionLoader(getActivity()).getSessions(mUrl,
                    new ConferenceSessionLoader.Listener() {
                        @Override
                        public void onSuccess(List<ConferenceSession> sessions) {
                            if (isResumed() && getActivity() != null) {
                                mConferenceSessions = sessions;
                                setupSessionList(sessions);
                            }
                        }

                        @Override
                        public void onError(VolleyError error) {
                            setEmptyText(getString(R.string.network_error));
                        }
                    }
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mConferenceSessions != null && !mConferenceSessions.isEmpty()) {
            outState.putParcelable(STATE_KEY_TRACK_SESSION, GsonParcer.wrap(mConferenceSessions));
        }
    }

    private void setupSessionList(List<ConferenceSession> sessions) {
        final ConferenceSessionAdapter adapter = new ConferenceSessionAdapter(getActivity());
        for (ConferenceSession session : sessions) {
            adapter.add(session);
        }

        mListView.setAdapter(adapter);
        setContentShown(true);
    }

}
