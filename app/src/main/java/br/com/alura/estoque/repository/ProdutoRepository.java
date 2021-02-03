package br.com.alura.estoque.repository;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    ProdutoService service;

    public ProdutoRepository(ProdutoDAO dao) {
        this.service =  new EstoqueRetrofit().getProdutoService();
        this.dao = dao;
    }

    public void buscaProdutos(DadosCarregadosCallBack<List<Produto>> callBack) {
        buscaProdutosInternos(callBack);
    }

    private void buscaProdutosInternos(DadosCarregadosCallBack<List<Produto>> callBack) {
        new BaseAsyncTask<>( dao::buscaTodos, resultado -> {
            callBack.quandoSucesso(resultado);
            //notifica que o dado esta pronto
            buscaProdutosNaAPI(callBack);
        }).execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosCallBack<List<Produto>> callBack) {
        Call<List<Produto>> call = service.buscaTodos();

        call.enqueue(new Callback<List<Produto>>() {
            @Override //executa na UI Thread Main Thread
            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
                if(response.isSuccessful()){
                    List<Produto> produtosNovos = response.body();

                    if(produtosNovos != null){
                        atualizaInterno(produtosNovos, callBack);
                    }
                }else{
                    callBack.quandoFalha("Falha de comunicacao");
                }
            }

            @Override
            public void onFailure(Call<List<Produto>> call, Throwable t) {
                callBack.quandoFalha("Falha de comunicação "+t.getMessage());
            }
        });


    }

    private void atualizaInterno(List<Produto> produtos, DadosCarregadosCallBack<List<Produto>> callBack) {
        new BaseAsyncTask<>( () ->
        {
            dao.salva(produtos);
            return dao.buscaTodos();
        }, resultado ->
        {
            callBack.quandoSucesso(produtos);
        }).execute();
    }

    public void salva(Produto produto,
                      DadosCarregadosCallBack<Produto> callback) {
        salvaNaApi(produto, callback);
    }

    private void salvaNaApi(Produto produto, DadosCarregadosCallBack<Produto> callback) {
        Call<Produto> call = service.salva(produto);

        //chamada assincrona da call
        call.enqueue(new Callback<Produto>() {
            //esses metodos sao executados na UI Thread
            @Override//comunicacao ok com o servidor
            public void onResponse(Call<Produto> call, Response<Produto> response) {

                if (response.isSuccessful()) {
                    Produto produtoSalvo = response.body();
                    if (produtoSalvo != null) {
                        salvaInterno(produto, callback);
                    }
                }else{
                // notifica uma falha
                callback.quandoFalha("Resposta não sucedida");
                }
            }

            @Override //comunicao erro com o servidor
            public void onFailure(Call<Produto> call, Throwable t) {
                //notifica uma falha
                callback.quandoFalha("Falha de comunicação "+t.getMessage());
            }
        });
    }

    private void salvaInterno(Produto produto, DadosCarregadosCallBack<Produto> callBack) {
        //salva primeiro na base
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, salvo -> {
            //notificar que o dado esta pronto
            callBack.quandoSucesso(salvo);
        }).execute();
    }

    public interface DadosCarregadosCallBack<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }

}
