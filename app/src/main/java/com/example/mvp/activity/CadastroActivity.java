package com.example.mvp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.mvp.R;
import com.example.mvp.helper.ConfiguracaoFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {


    private Button botaoAcessar;
    private EditText campoEmail,campoSenha;
    private Switch tipoAcesso;

    private FirebaseAuth autenticacao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializaComponentes();
        autenticacao = ConfiguracaoFirebase.getFirebaseAutenticacao();
        botaoAcessar.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String email=campoEmail.getText().toString();
                String senha=campoSenha.getText().toString();


                if (!email.isEmpty() ){
                    if(!senha.isEmpty()){
                        //Verifica o switch
                        if(tipoAcesso.isChecked()){
                            //cadastro
                            autenticacao.createUserWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if( task.isSuccessful() ){

                                        Toast.makeText(CadastroActivity.this,
                                                "Cadastro realizado com sucesso!",
                                                Toast.LENGTH_SHORT).show();

                                        //direcionar para tela principal do App

                                    }else {
                                        String erroExcecao = "";

                                        try {
                                            throw task.getException();
                                        }catch (FirebaseAuthWeakPasswordException e){
                                            erroExcecao = "Digite uma senha mais forte!";
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "E-mail inválido.";
                                        }catch (FirebaseAuthUserCollisionException e){
                                            erroExcecao = "Conta já existente.";
                                        }catch (Exception e){
                                            erroExcecao = "Ao cadastrar o usuário: " + e.getMessage();
                                            e.printStackTrace();
                                        }

                                        Toast.makeText(CadastroActivity.this,
                                                "Erro " + erroExcecao ,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                        else{
                            //login

                            autenticacao.signInWithEmailAndPassword(
                                    email, senha
                            ).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if( task.isSuccessful() ){

                                        Toast.makeText(CadastroActivity.this,
                                                "Login realizado com sucesso.",
                                                Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), AnunciosActivity.class));

                                    }else {
                                        String erroExcecao = "";

                                        try {
                                            throw task.getException();
                                        }catch (FirebaseAuthInvalidCredentialsException e){
                                            erroExcecao = "Senha inválida.";

                                        }catch (FirebaseAuthInvalidUserException e){
                                            erroExcecao = "E-mail não cadastrado.";

                                        }catch (Exception e){
                                            erroExcecao = "Ao cadastrar o usuário: " + e.getMessage();
                                            e.printStackTrace();
                                        }


                                        Toast.makeText( CadastroActivity.this,
                                                "Erro ao fazer login " + erroExcecao ,
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }

                    else{ Toast.makeText(CadastroActivity.this,
                            "Preencha a senha",
                            Toast.LENGTH_LONG).show();


                    }

                }else{
                    Toast.makeText(CadastroActivity.this,
                            "Preencha o E-mail",
                            Toast.LENGTH_LONG).show();

                }
            }
        });
    }

    private void inicializaComponentes() {

        campoEmail= findViewById(R.id.editCadastroEmail);
        campoSenha=findViewById(R.id.editCadastroSenha);
        botaoAcessar=findViewById(R.id.buttonAcesso);
        tipoAcesso=findViewById(R.id.switchAcesso);


    }
}