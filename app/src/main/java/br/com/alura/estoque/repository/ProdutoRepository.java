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

    public void buscaProdutos(DadosCarregadosListener<List<Produto>> listener) {
        buscaProdutosInternos(listener);
    }

    private void buscaProdutosInternos(DadosCarregadosListener<List<Produto>> listener) {
        new BaseAsyncTask<>( dao::buscaTodos, resultado -> {
            listener.quandoCarregados(resultado);
            //notifica que o dado esta pronto
            buscaProdutosNaAPI(listener);
        }).execute();
    }

    private void buscaProdutosNaAPI(DadosCarregadosListener<List<Produto>> listener) {
        Call<List<Produto>> call = service.buscaTodos();
        new BaseAsyncTask<>( () ->{
            try{
                Response<List<Produto>> resposta = call.execute();
                List<Produto> produtosNovos = resposta.body();
                dao.salva(produtosNovos);
            }catch(IOException e){
                e.printStackTrace();
            }
            return dao.buscaTodos();
        }, produtosNovos -> {
            listener.quandoCarregados(produtosNovos);
            //notifica que o dado esta pronto
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

    public interface DadosCarregadosListener<T>{
        void quandoCarregados(T resultado);
    }

    public interface DadosCarregadosCallBack<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String erro);
    }

}
