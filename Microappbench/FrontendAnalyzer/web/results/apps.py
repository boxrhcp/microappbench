from django.apps import AppConfig
from django.views.generic import TemplateView
from .urls import urlpatterns
from django.urls import path as ps
from .aggregate_results import stats
import os
import pandas as pd


class ResultsConfig(AppConfig):
    name = 'results'

    def ready(self):
        path = os.path.dirname(
            os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
        data = pd.read_json(path + '/results.json')

        for pattern in data['issuePatterns']:
            stats(pattern)

        base = os.path.dirname(os.path.abspath(__file__))
        file_list = os.listdir(base + '/templates/results/files')
        for file_name in file_list:
            urlpatterns.append(ps(file_name, TemplateView.as_view(template_name='results/files/' + file_name)))
