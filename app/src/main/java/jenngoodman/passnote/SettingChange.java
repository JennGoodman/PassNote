package jenngoodman.passnote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingChange extends ActionBarActivity {

    MessageHomeFragment messageHomeFragment;
    EditText displayName;
    String s = "";
    Button submitChangesBtn;

    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings_change);
        displayName = (EditText) findViewById(R.id.editTextDN);
        submitChangesBtn = (Button) findViewById(R.id.change_dn);


        submitChangesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s = displayName.getText().toString();
                //   if(savedInstanceState==null){

                //       getSupportFragmentManager().beginTransaction()
                //  .add(R.id.containers_root, createCustomFragment()).commit();
            }

            //   Intent myIntent= new Intent(SettingChange.this,MessageHomeFragment.class);
            //     myIntent.putExtra("sendme",s);
            //   startActivity(myIntent);


        });
    }

    private Fragment createCustomFragment() {

        Bundle bundle = new Bundle();
        bundle.putString(s, "sendme");

        MessageHomeFragment messageHomeFragment1 = new MessageHomeFragment();
        messageHomeFragment1.setArguments(bundle);
        return createCustomFragment();
    }


}
