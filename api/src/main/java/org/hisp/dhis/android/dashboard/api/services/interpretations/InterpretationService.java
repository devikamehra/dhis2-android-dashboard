package org.hisp.dhis.android.dashboard.api.services.interpretations;

import org.hisp.dhis.android.dashboard.api.models.Models;
import org.hisp.dhis.android.dashboard.api.models.common.Access;
import org.hisp.dhis.android.dashboard.api.models.common.meta.State;
import org.hisp.dhis.android.dashboard.api.models.dashboard.DashboardItem;
import org.hisp.dhis.android.dashboard.api.models.interpretation.Interpretation;
import org.hisp.dhis.android.dashboard.api.models.interpretation.InterpretationComment;
import org.hisp.dhis.android.dashboard.api.models.interpretation.InterpretationElement;
import org.hisp.dhis.android.dashboard.api.models.user.User;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.DateTimeManager;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.ResourceType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by arazabishov on 8/27/15.
 */
public final class InterpretationService implements IInterpretationsService {

    private final IInterpretationElementService mInterpretationElementService;

    public InterpretationService(IInterpretationElementService service) {
        mInterpretationElementService = service;
    }

    /**
     * Creates comment for given interpretation. Comment is assigned to given user.
     *
     * @param interpretation Interpretation to associate comment with.
     * @param user           User who wants to create comment.
     * @param text           The actual content of comment.
     * @return Intrepretation comment.
     */
    @Override
    public InterpretationComment addComment(Interpretation interpretation, User user, String text) {
        DateTime lastUpdated = DateTimeManager.getInstance()
                .getLastUpdated(ResourceType.INTERPRETATIONS);

        InterpretationComment comment = new InterpretationComment();
        comment.setCreated(lastUpdated);
        comment.setLastUpdated(lastUpdated);
        comment.setAccess(Access.provideDefaultAccess());
        comment.setText(text);
        comment.setState(State.TO_POST);
        comment.setUser(user);
        comment.setInterpretation(interpretation);
        return comment;
    }

    /**
     * This method allows to create interpretation from: chart, map,
     * reportTable. Please note, it won't work for data sets.
     * <p/>
     * Note, model won't be saved to database automatically. You have to call .save()
     * both on interpretation and interpretation elements of current object.
     *
     * @param item DashboardItem which will represent content of interpretation.
     * @param user User who associated with Interpretation.
     * @param text Interpretation text written by user.
     * @return new Interpretation.
     */
    @Override
    public Interpretation createInterpretation(DashboardItem item, User user, String text) {
        DateTime lastUpdated = DateTimeManager.getInstance()
                .getLastUpdated(ResourceType.INTERPRETATIONS);

        Interpretation interpretation = new Interpretation();
        interpretation.setCreated(lastUpdated);
        interpretation.setLastUpdated(lastUpdated);
        interpretation.setAccess(Access.provideDefaultAccess());
        interpretation.setText(text);
        interpretation.setState(State.TO_POST);
        interpretation.setUser(user);

        switch (item.getType()) {
            case Interpretation.TYPE_CHART: {
                InterpretationElement element = mInterpretationElementService
                        .createInterpretationElement(interpretation, item.getChart(), Interpretation.TYPE_CHART);
                interpretation.setType(Interpretation.TYPE_CHART);
                interpretation.setChart(element);
                break;
            }
            case Interpretation.TYPE_MAP: {
                InterpretationElement element = mInterpretationElementService
                        .createInterpretationElement(interpretation, item.getMap(), Interpretation.TYPE_MAP);
                interpretation.setType(Interpretation.TYPE_MAP);
                interpretation.setMap(element);
                break;
            }
            case Interpretation.TYPE_REPORT_TABLE: {
                InterpretationElement element = mInterpretationElementService
                        .createInterpretationElement(interpretation, item.getReportTable(), Interpretation.TYPE_REPORT_TABLE);
                interpretation.setType(Interpretation.TYPE_REPORT_TABLE);
                interpretation.setReportTable(element);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported DashboardItem type");
            }
        }

        return interpretation;
    }

    @Override
    public void updateInterpretationText(Interpretation interpretation, String text) {
        interpretation.setText(text);

        if (interpretation.getState() != State.TO_DELETE &&
                interpretation.getState() != State.TO_POST) {
            interpretation.setState(State.TO_UPDATE);
        }

        Models.interpretations().save(interpretation);
    }

    @Override
    public void deleteInterpretation(Interpretation interpretation) {
        if (State.TO_POST.equals(interpretation.getState())) {
            Models.interpretations().delete(interpretation);
        } else {
            interpretation.setState(State.TO_DELETE);
            Models.interpretations().save(interpretation);
        }
    }

    /**
     * Convenience method which allows to set InterpretationElements
     * to Interpretation depending on their mime-type.
     *
     * @param elements List of interpretation elements.
     */
    @Override
    public void setInterpretationElements(Interpretation interpretation, List<InterpretationElement> elements) {
        if (elements == null || elements.isEmpty()) {
            return;
        }

        if (interpretation.getType() == null) {
            return;
        }

        if (interpretation.getType().equals(Interpretation.TYPE_DATA_SET_REPORT)) {
            for (InterpretationElement element : elements) {
                switch (element.getType()) {
                    case InterpretationElement.TYPE_DATA_SET: {
                        interpretation.setDataSet(element);
                        break;
                    }
                    case InterpretationElement.TYPE_PERIOD: {
                        interpretation.setPeriod(element);
                        break;
                    }
                    case InterpretationElement.TYPE_ORGANISATION_UNIT: {
                        interpretation.setOrganisationUnit(element);
                        break;
                    }
                }
            }
        } else {
            switch (interpretation.getType()) {
                case InterpretationElement.TYPE_CHART: {
                    interpretation.setChart(elements.get(0));
                    break;
                }
                case InterpretationElement.TYPE_MAP: {
                    interpretation.setMap(elements.get(0));
                    break;
                }
                case InterpretationElement.TYPE_REPORT_TABLE: {
                    interpretation.setReportTable(elements.get(0));
                    break;
                }
            }
        }
    }

    /**
     * Convenience method which allows to get
     * interpretation elements assigned to current object.
     *
     * @return List of interpretation elements.
     */
    @Override
    public List<InterpretationElement> getInterpretationElements(Interpretation interpretation) {
        List<InterpretationElement> elements = new ArrayList<>();

        switch (interpretation.getType()) {
            case Interpretation.TYPE_CHART: {
                elements.add(interpretation.getChart());
                break;
            }
            case Interpretation.TYPE_MAP: {
                elements.add(interpretation.getMap());
                break;
            }
            case Interpretation.TYPE_REPORT_TABLE: {
                elements.add(interpretation.getReportTable());
                break;
            }
            case Interpretation.TYPE_DATA_SET_REPORT: {
                elements.add(interpretation.getDataSet());
                elements.add(interpretation.getPeriod());
                elements.add(interpretation.getOrganisationUnit());
                break;
            }
        }

        return elements;
    }
}