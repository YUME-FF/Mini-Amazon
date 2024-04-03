from django import forms
from django.contrib.auth.models import User
from django.contrib.auth.forms import UserCreationForm
from .models import UserAccount
from django.forms.fields import DateInput, DateField, NumberInput
from django.forms import EmailField


class UserUpdateForm(forms.ModelForm):
    email = forms.EmailField()

    class Meta:
        model = User
        fields = ['username', 'email']


class UserCreationForm(UserCreationForm):
    email = EmailField(required=True)

    class Meta:
        model = User
        fields = ["username", "email", "password1", "password2"]


class EditProfileForm(forms.Form):
    email = forms.EmailField()
    username = forms.CharField(max_length=100, required=True)


class EditDeliveryProfileForm(forms.Form):
    phone = forms.IntegerField()
    creditCard = forms.IntegerField()


class addDeliveryAddressForm(forms.Form):
    addressName = forms.CharField(max_length=100, required=True)
    address_x = forms.IntegerField(required=True)
    address_y = forms.IntegerField(required=True)
