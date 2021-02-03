package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class CallBackComRetorno<T> implements Callback<T> {

    public static final String MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA = "Resposta não sucedida";
    private final RespostaCallBack<T> callback;

    public CallBackComRetorno(RespostaCallBack<T> callback) {
        this.callback = callback;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<T> call, Response<T> response) {
        if(response.isSuccessful()){
            T resultado = response.body();
            if(resultado != null){
                //notifica que tem resposta com sucesso
                callback.quandoSucesso(resultado);
            }
        }else{
            //notifica falha
            callback.quandoFalha(MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA);
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<T> call, Throwable t) {
        //notifica falha
        callback.quandoFalha(MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA+t.getMessage());
    }

    public interface RespostaCallBack<T>{
        void quandoSucesso(T resultado);
        void quandoFalha(String mensagemErro);
    }
}
