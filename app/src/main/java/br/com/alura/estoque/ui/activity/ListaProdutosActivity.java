package br.com.alura.estoque.ui.activity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import br.com.alura.estoque.R;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.repository.ProdutoRepository;
import br.com.alura.estoque.ui.dialog.EditaProdutoDialog;
import br.com.alura.estoque.ui.dialog.SalvaProdutoDialog;
import br.com.alura.estoque.ui.recyclerview.adapter.ListaProdutosAdapter;

public class ListaProdutosActivity extends AppCompatActivity {

    private static final String TITULO_APPBAR = "Lista de produtos";
    private ListaProdutosAdapter adapter;
    ProdutoRepository repository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produtos);
        setTitle(TITULO_APPBAR);

        configuraListaProdutos();
        configuraFabSalvaProduto();
        
        repository = new ProdutoRepository(this);
        buscaProdutos();
    }

    private void buscaProdutos() {
        repository.buscaProdutos(new ProdutoRepository.DadosCarregadosCallBack<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> resultado) {
                adapter.atualiza(resultado);
            }

            @Override
            public void quandoFalha(String erro) {
                Toast.makeText(ListaProdutosActivity.this, "Nao foi possivel carregar os produtos", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void configuraListaProdutos() {
        RecyclerView listaProdutos = findViewById(R.id.activity_lista_produtos_lista);
        adapter = new ListaProdutosAdapter(this, this::abreFormularioEditaProduto);
        listaProdutos.setAdapter(adapter);
        adapter.setOnItemClickRemoveContextMenuListener(
                (posicao, produto) ->
                        repository.remove(produto,
                                new ProdutoRepository.DadosCarregadosCallBack<Void>() {
                                    @Override
                                    public void quandoSucesso(Void resultado) {
                                        adapter.remove(posicao);
                                    }

                                    @Override
                                    public void quandoFalha(String erro) {
                                        Toast.makeText(ListaProdutosActivity.this,
                                                "Não foi possivel remover o produto",
                                                Toast.LENGTH_LONG ).show();
                                    }
        }));
    }



    private void configuraFabSalvaProduto() {
        FloatingActionButton fabAdicionaProduto = findViewById(R.id.activity_lista_produtos_fab_adiciona_produto);
        fabAdicionaProduto.setOnClickListener(v -> abreFormularioSalvaProduto());
    }

    private void abreFormularioSalvaProduto() {
        new SalvaProdutoDialog(this, produtoCriado -> {
            repository.salva(produtoCriado, new ProdutoRepository.DadosCarregadosCallBack<Produto>() {
                        @Override
                        public void quandoSucesso(Produto produtoSalvo) {
                            adapter.adiciona(produtoSalvo);
                        }

                        @Override
                        public void quandoFalha(String erro) {
                            Toast.makeText(ListaProdutosActivity.this, "Não foi possivel salvar o produto", Toast.LENGTH_LONG ).show();
                        }
            });
        }).mostra();
    }

    private void abreFormularioEditaProduto(int posicao, Produto produto) {
        new EditaProdutoDialog(this, produto,
                produtoCriado -> repository.edita(produtoCriado,
                        new ProdutoRepository.DadosCarregadosCallBack<Produto>() {
                    @Override
                    public void quandoSucesso(Produto produtoEditado) {
                       adapter.edita(posicao, produtoCriado);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        Toast.makeText(ListaProdutosActivity.this, "Nao foi possivel editar o produto", Toast.LENGTH_LONG).show();
                    }
                }))
                .mostra();
    }




}
