package com.example.mvp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
//import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.example.mvp.R;
import com.example.mvp.helper.ConfiguracaoFirebase;
import com.example.mvp.helper.Permissoes;
import com.example.mvp.model.Anuncio;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.santalu.maskara.widget.MaskEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;

public class CadastrarAnuncioActivity extends AppCompatActivity
        implements View.OnClickListener {

    private EditText campoTitulo;
    //            ,campoDescricao;
    private ImageView imagem1,imagem2, imagem3;
    private Spinner campoEstado, campoCategoria;
    //    private CurrencyEditText campoValor;
    private MaskEditText campoTelefone;
    private Anuncio anuncio;
    private StorageReference storage;
    private AlertDialog dialog;

    private String[] permissoes = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    private List<String> listaFotosRecuperadas = new ArrayList<>();
    private List<String> listaURLFotos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_anuncio);

        storage = ConfiguracaoFirebase.getFirebaseStorage();

        Permissoes.validarPermissoes(permissoes, this, 1);

        inicializarComponentes();
        carregarDadosSpinner();
    }

    public void salvarAnuncio(){

        dialog = new SpotsDialog.Builder()
                .setContext( this )
                .setMessage("Salvando Livro")
                .setCancelable( false )
                .build();
        dialog.show();
//Salvar imagem no storage

        for(int i=0; i<listaFotosRecuperadas.size(); i++){

            String urlImagem = listaFotosRecuperadas.get(i);
            int tamanhoLista= listaFotosRecuperadas.size();

            salvarFotoStorage(urlImagem,tamanhoLista,i);




        }

    }

    private void salvarFotoStorage(String urlString, final int totalFotos, int contador){

        StorageReference imagemAnuncio = storage.child("imagens")
                .child("anuncios")
                .child(anuncio.getIdAnuncio())
                .child("imagem"+contador);
        UploadTask uploadTask=imagemAnuncio.putFile(Uri.parse(urlString));

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override

            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri firebaseUrl = taskSnapshot.getDownloadUrl();
                String urlConvertida = firebaseUrl.toString();

                listaURLFotos.add(urlConvertida);
                if ( totalFotos == listaURLFotos.size()){

                    anuncio.setFotos(listaURLFotos);
                    anuncio.salvar();

                    dialog.dismiss();
                    finish();

                }



            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                exibirMensagemErro("Falha ao fazer upload");
                Log.i("INFO","Falha ao fazer upload: "+ e.getMessage());
            }
        });
    }

    private Anuncio configurarAnuncio(){

        String estado = campoEstado.getSelectedItem().toString();
        String categoria = campoCategoria.getSelectedItem().toString();
        String titulo = campoTitulo.getText().toString();
//      String valor = campoValor.getText().toString();
        String telefone = campoTelefone.getText().toString();
//        String descricao = campoDescricao.getText().toString();

        Anuncio anuncio = new Anuncio();
        anuncio.setEstado(estado);
        anuncio.setCategoria( categoria);
        anuncio.setTitulo(titulo);
//        anuncio.setValor(valor);
        anuncio.setTelefone(telefone);
//        anuncio.setDescricao(descricao);

        return anuncio;




    }


    public void validarDadosAnuncio(View view) {

        String fone = "";
        anuncio = configurarAnuncio();
//        String valor = String.valueOf(campoValor.getRawValue());

        if ( campoTelefone.getText() != null ){
            fone = campoTelefone.getText().toString();
        }


        if (listaFotosRecuperadas.size() >= 1) {
            if( !anuncio.getEstado().isEmpty() ){
                if ( !anuncio.getCategoria().isEmpty() ){
                    if ( !anuncio.getTitulo().isEmpty() ){

                        if ( !anuncio.getTelefone().isEmpty() && fone.length() >=10 ){

                            salvarAnuncio();


                        }else{
                            exibirMensagemErro("Preencha um número de telefone válido.");
                        }
                    } else{



                        exibirMensagemErro("Preencha o campo título!");
                    }
                }else{
                    exibirMensagemErro("Preencha o campo gênero!");
                }
            }else{
                exibirMensagemErro("Preencha o campo bairro!");
            }
        } else {
            exibirMensagemErro("Selecione ao menos uma foto!");
        }
    }

    private void exibirMensagemErro (String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
    }






    @Override
    public void onClick(View v) {

        switch ( v.getId() ){
            case R.id.imageCadastro1 :
                escolherImagem(1);
                break;
            case R.id.imageCadastro2 :
                escolherImagem(2);
                break;
            case R.id.imageCadastro3 :
                escolherImagem(3);
                break;
        }

    }

    public void escolherImagem( int requestCode){

        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, requestCode);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if( resultCode == Activity.RESULT_OK ){

            Uri imagemSelecionada = data.getData();
            String caminhoImagem = imagemSelecionada.toString();

            if( requestCode == 1 ){
                imagem1.setImageURI( imagemSelecionada );
            } else if ( requestCode == 2 ){
                imagem2.setImageURI( imagemSelecionada );
            } else if( requestCode == 3){
                imagem3.setImageURI( imagemSelecionada);
            }

            listaFotosRecuperadas.add( caminhoImagem );



        }

    }

    private void carregarDadosSpinner(){

       /* String[] estados = new String[]{
                "AC","AL","AP","AM","BA","CE","DF","ES","GO","MA", "MT", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP", "SE", "TO"
        };*/

        //Configura spinner de estados
        String[] bairros = getResources().getStringArray(R.array.bairros);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                bairros
        );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoEstado.setAdapter( adapter );

        //Configura spinner de categorias
        String[] categorias = getResources().getStringArray(R.array.categorias);
        ArrayAdapter<String> adapterCategoria = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item,
                categorias
        );
        adapterCategoria.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        campoCategoria.setAdapter( adapterCategoria );

    }

    private void inicializarComponentes(){

        campoTitulo = findViewById(R.id.editTitulo);
//        campoDescricao = findViewById(R.id.editDescricao);
//        campoValor = findViewById(R.id.editValor);
        campoTelefone = findViewById(R.id.editTelefone);
        campoEstado = findViewById(R.id.spinnerEstado);
        campoCategoria = findViewById(R.id.spinnerCategoria);
        imagem1 = findViewById(R.id.imageCadastro1);
        imagem2 = findViewById(R.id.imageCadastro2);
        imagem3 = findViewById(R.id.imageCadastro3);
        imagem1.setOnClickListener(this);
        imagem2.setOnClickListener(this);
        imagem3.setOnClickListener(this);
//


//        Locale locale = new Locale("pt", "BR");
//        campoValor.setLocale(locale);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for( int permissaoResultado : grantResults ){
            if( permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao();
            }
        }

    }

    private void alertaValidacaoPermissao(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setMessage("Para utilizar o APP é necessário aceitar as permissões");
        builder.setCancelable(false);
        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

}