from django import forms
from django.forms.fields import NumberInput


class PurchaseForm(forms.Form):
    quantity = forms.IntegerField(
        widget=NumberInput(attrs={'min': 1, 'max': 100}), required=True, label='Quantity')
    address_x = forms.CharField(
        label='Address x', max_length=100, required=True)
    address_y = forms.CharField(
        label='Address y', max_length=100, required=True)
