package projetofinal.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.projetofinal.databinding.FragmentDestaqueBinding;

public class DestaqueFragment extends Fragment {

    private FragmentDestaqueBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDestaqueBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Pede as informações do destaque para a Activity principal
        if (getActivity() instanceof MainClienteActivity) {
            MainClienteActivity activity = (MainClienteActivity) getActivity();
            MainClienteActivity.ProdutoDestaque destaque = activity.getProdutoDestaqueDoDia();

            // Preenche a tela com os dados recebidos
            binding.textViewNomeDestaque.setText(destaque.nome);
            binding.textViewDescricaoDestaque.setText(destaque.descricao);
            Glide.with(this).load(destaque.drawableId).into(binding.imageViewDestaque);

            // A lógica de compra e logout foi removida daqui para simplificar
            // e centralizar na Activity principal e na tela de Perfil.
            binding.btnComprarDestaque.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}