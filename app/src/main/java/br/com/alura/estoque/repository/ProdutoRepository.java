package br.com.alura.estoque.repository;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.BaseCallBack;
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

        call.enqueue(new BaseCallBack<>(new BaseCallBack.RespostaCallBack<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtosNovos) {
                atualizaInterno(produtosNovos, callBack);
            }

            @Override
            public void quandoFalha(String mensagemErro) {
                callBack.quandoFalha(mensagemErro);
            }
        }));
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
        call.enqueue(new BaseCallBack<>(new BaseCallBack.RespostaCallBack<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                salvaInterno(produto, callback);
            }

            @Override
            public void quandoFalha(String mensagemErro) {
                callback.quandoFalha(mensagemErro);
            }
        }));
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
