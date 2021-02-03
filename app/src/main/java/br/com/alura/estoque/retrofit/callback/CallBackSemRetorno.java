package br.com.alura.estoque.retrofit.callback;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

public class CallBackSemRetorno implements Callback<Void> {

    public static final String MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA = "Resposta não sucedida";
    private final RespostaCallBack callBack;

    public CallBackSemRetorno(RespostaCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    @EverythingIsNonNull
    public void onResponse(Call<Void> call, Response<Void> response) {
        if(response.isSuccessful()){
            //notifica sucesso
            callBack.quandoSucesso();
        }else{
            //notifica falha
            callBack.quandoFalha(MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA);
        }
    }

    @Override
    @EverythingIsNonNull
    public void onFailure(Call<Void> call, Throwable t) {
        //notifica falha
        callBack.quandoFalha(MENSAGEM_ERRO_RESPOSTA_NAO_SUCEDIDA+t.getMessage());
    }

    public interface RespostaCallBack{
        void quandoSucesso();
        void quandoFalha(String mensagemErro);
    }
}
