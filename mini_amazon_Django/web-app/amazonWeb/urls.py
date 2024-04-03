from django.urls import path

from . import views

urlpatterns = [
    path('', views.userHome, name='UserHome'),
    path('purchase/<int:product_id>', views.purchase, name='purchase'),
    path('myCategory/<int:category_id>', views.myCategory, name='myCategory'),
    path('orderHistory', views.orderHistory, name='orderHistory'),
]
