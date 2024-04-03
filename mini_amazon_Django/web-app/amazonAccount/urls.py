from django.urls import path

from . import views

urlpatterns = [
    path('', views.home, name='home'),
    path('login/', views.login, name='login'),
    path('logout/', views.logout, name='logout'),
    path('createAccount/', views.createAccount, name='createAccount'),
    path('profile/', views.profile, name='profile'),
    path('editProfile/', views.editProfile, name='editProfile'),
    path('editDeliveryProfile/', views.editDeliveryProfile,
         name='editDeliveryProfile'),
    path('addDeliveryAddress/', views.addDeliveryAddress,
         name='addDeliveryAddress'),
    path('editDeliveryAddress/', views.editDeliveryAddress,
         name='editDeliveryAddress'),
]
