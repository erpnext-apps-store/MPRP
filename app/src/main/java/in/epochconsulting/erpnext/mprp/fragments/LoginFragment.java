package in.epochconsulting.erpnext.mprp.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;


import org.json.JSONObject;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.Map;

import in.epochconsulting.erpnext.mprp.activity.Home;
import in.epochconsulting.erpnext.mprp.common.BaseFragment;
import in.epochconsulting.erpnext.mprp.R;
import in.epochconsulting.erpnext.mprp.utils.Constants;
import in.epochconsulting.erpnext.mprp.utils.CustomUrl;
import in.epochconsulting.erpnext.mprp.utils.PersistentCookieStoreManager;
import in.epochconsulting.erpnext.mprp.utils.Utility;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends BaseFragment {

    Button loginButton;

    JSONObject jsonObject;
    boolean isaNewLogin;
    String IS_NEW_LOGIN;

    EditText username_entry;
    EditText password_entry;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View loginFragment = inflater.inflate(R.layout.fragment_login, container, false);
        username_entry = loginFragment.findViewById(R.id.username_entry);
        password_entry = loginFragment.findViewById(R.id.password_entry);
        loginButton = loginFragment.findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override

                public void onClick(View v) {

                showProgress();
                requestLogin();

                }
            });

        CookieManager cookieManager = new CookieManager(new PersistentCookieStoreManager(context), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        CookieHandler.setDefault(cookieManager);
        // Inflate the layout for this fragment
        return loginFragment;
    }

    private void requestLogin() {
        RequestQueue requestQueue = Volley.newRequestQueue(LoginFragment.super.context);

        String  myUrl = Utility.getInstance().buildUrl(CustomUrl.API_METHOD,null,CustomUrl.LOGIN_URL);
        System.out.println("The url to login is : "+myUrl);

        StringRequest stringRequest = new StringRequest(Request.Method.POST,myUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                hideProgress();
                String loginResponse = null;

                try {
                    if(response!=null){
                        jsonObject = new JSONObject(response);
                    }
                    loginResponse = jsonObject.get("message").toString();
                    if (loginResponse!=null && loginResponse.equalsIgnoreCase(Constants.LOGIN_RESPONSE)) {
                        String loggedUser = jsonObject.get("full" +
                                "_name").toString();
                        String successmsg = Constants.LOGIN_SUCCESS + " " + loggedUser;
                        Toast.makeText(LoginFragment.super.context, successmsg, Toast.LENGTH_LONG).show();
                        isaNewLogin = true;
                        SharedPreferences.Editor editor = getActivity().getSharedPreferences(IS_NEW_LOGIN, MODE_PRIVATE).edit();
                        editor.putBoolean("IsNewUser",isaNewLogin);

                        editor.apply();
                        editor.commit();

                        //added on 25th Oct 2017 to tie user down to a warehouse
                        startRequestActivity();

                    } else {
                        Toast.makeText(LoginFragment.super.context, R.string.login_failed, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    Log.e("ERROR",e.toString());
                }


            }


        }//end of sucess response
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                hideProgress();
                Toast.makeText(LoginFragment.super.context, "Login Failed, Server Error: "+error.toString(), Toast.LENGTH_LONG).show();


            }
        })
                //end of error response and stringRequest params
        {
            //This is for providing body for Post request@Override
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> mapObject = new HashMap<>();
                mapObject.put(Constants.KEY_NAME, username_entry.getText().toString());
                mapObject.put(Constants.KEY_PASS, password_entry.getText().toString());
                return mapObject;

            }
        };


        stringRequest.setRetryPolicy(new DefaultRetryPolicy(30 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        requestQueue.add(stringRequest);

    }

    public void startRequestActivity()
    {
        Intent startRequestActivity = new Intent(LoginFragment.super.getContext(),Home.class);
        startActivity(startRequestActivity);
    }

}
