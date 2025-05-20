package ma.ensa.ebankingver1.service;


import ma.ensa.ebankingver1.DTO.AccountOperationDTO;
import ma.ensa.ebankingver1.DTO.ClientDTO;
import ma.ensa.ebankingver1.DTO.CurrentBankAccountDTO;
import ma.ensa.ebankingver1.DTO.SavingBankAccountDTO;
import ma.ensa.ebankingver1.model.AccountOperation;
import ma.ensa.ebankingver1.model.CurrentAccount;
import ma.ensa.ebankingver1.model.SavingAccount;
import ma.ensa.ebankingver1.model.User;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
@Service
//this class is used to convert between DTOs and entities for security
public class BankAccountMapperImpl {
    public ClientDTO fromClient(User Client){
        ClientDTO ClientDTO = new ClientDTO();
        BeanUtils.copyProperties(Client,ClientDTO);
        return ClientDTO;
    }

    public User fromClientDTO(ClientDTO ClientDTO){
        User Client = new User();
        BeanUtils.copyProperties(ClientDTO,Client);
        return Client;
    }

    public SavingBankAccountDTO fromSavingBankAccount(SavingAccount savingAccount){
        SavingBankAccountDTO savingBankAccountDTO = new SavingBankAccountDTO();
        BeanUtils.copyProperties(savingAccount,savingBankAccountDTO);
        savingBankAccountDTO.setClient(fromClient(savingAccount.getClient()));
        savingBankAccountDTO.setType(savingAccount.getClass().getSimpleName());
        return savingBankAccountDTO;
    }

    public SavingAccount fromSavingAccountBankAccountDTO(SavingBankAccountDTO savingBankAccountDTO){
        SavingAccount savingAccount = new SavingAccount();
        BeanUtils.copyProperties(savingBankAccountDTO,savingAccount);
        savingAccount.setClient(fromClientDTO(savingBankAccountDTO.getClient()));
        return savingAccount;
    }

    public CurrentBankAccountDTO fromCurrentBankAccount(CurrentAccount currentAccount){
        CurrentBankAccountDTO currentBankAccountDTO = new CurrentBankAccountDTO();
        BeanUtils.copyProperties(currentAccount, currentBankAccountDTO);
        currentBankAccountDTO.setClient(fromClient(currentAccount.getClient()));
        currentBankAccountDTO.setType(currentAccount.getClass().getSimpleName());
        return currentBankAccountDTO;
    }

    public CurrentAccount fromCurrentAccountBankAccountDTO(CurrentBankAccountDTO currentBankAccountDTO){
        CurrentAccount currentAccount = new CurrentAccount();
        BeanUtils.copyProperties(currentBankAccountDTO,currentAccount);
        currentAccount.setClient(fromClientDTO(currentBankAccountDTO.getClient()));
        return currentAccount;
    }

    public AccountOperationDTO fromAccountOperation(AccountOperation accountOperation){
        AccountOperationDTO accountOperationDTO = new AccountOperationDTO();
        BeanUtils.copyProperties(accountOperation,accountOperationDTO);

        return accountOperationDTO;
    }


}
