package br.com.alura.estoque.repository;

import android.os.AsyncTask;

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
                      DadosCarregadosListener<Produto> listener) {

        Call<Produto> call = service.salva(produto);

        //chamada assincrona da call
        call.enqueue(new Callback<Produto>() {
            //esses metodos sao executados na UI Thread
            @Override//comunicacao ok com o servidor
            public void onResponse(Call<Produto> call, Response<Produto> response) {

                Produto produtoSalvo = response.body();
                //salva primeiro na base
                new BaseAsyncTask<>(() -> {
                    long id = dao.salva(produto);
                    return dao.buscaProduto(id);
                }, salvo ->
                        //notificar que o dado esta pronto
                        listener.quandoCarregados(salvo)
                ).execute();

                //notificamos que o produto foi salvo
                listener.quandoCarregados(produtoSalvo);
            }

            @Override //comunicao erro com o servidor
            public void onFailure(Call<Produto> call, Throwable t) {

            }
        });


    }

    public interface DadosCarregadosListener<T>{
        void quandoCarregados(T resultado);
    }

}
