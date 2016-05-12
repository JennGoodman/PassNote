package jenngoodman.passnote;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(MainActivity.this, WifiServiceDiscovery.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            switch (item.getItemId()) {
                case R.id.action_settings:
                    startActivity(new Intent(this, SettingChange.class));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void startChat(View v) {
        startActivity(new Intent(MainActivity.this, WifiServiceDiscovery.class));
    }
}